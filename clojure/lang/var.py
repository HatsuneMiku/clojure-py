import contextlib

from clojure.lang.aref import ARef
from clojure.lang.atomicreference import AtomicReference
from clojure.lang.cljexceptions import (ArityException,
                                        IllegalStateException)
from clojure.lang.cljkeyword import Keyword
from clojure.lang.ifn import IFn
from clojure.lang.iprintable import IPrintable
from clojure.lang.persistenthashmap import EMPTY
from clojure.lang.persistentarraymap import create
from clojure.lang.settable import Settable
from clojure.lang.symbol import Symbol
from clojure.lang.threadutil import ThreadLocal, currentThread

privateKey = Keyword("private")
macrokey = Keyword("macro")
STATIC_KEY = Keyword("static")
dvals = ThreadLocal()
privateMeta = create([privateKey, True])
UNKNOWN = Symbol("UNKNOWN")


class Var(ARef, Settable, IFn, IPrintable):
    def __init__(self, *args):
        """Var initializer

        Valid calls:
        - Var(namespace, symbol, root)
        - Var(namespace, symbol) -- unbound Var
        - Var(root) -- anonymous Var
        - Var() -- anonymous, unbound Var
        """
        self.ns = args[0] if len(args) >= 2 else None
        self.sym = args[1] if len(args) >= 2 else None
        root = args[-1] if len(args) % 2 else UNKNOWN
        self.root = AtomicReference(root if root != UNKNOWN else Unbound(self))
        self.threadBound = False
        self._meta = EMPTY
        self.dynamic = False
        self.public = True

    def setDynamic(self, val=True):
        self.dynamic = val
        return self

    def isDynamic(self):
        return self.dynamic
        
    def setPublic(self, public = True):
        self.public = public
        
    def isPublic(self):
        return self.public
        
    def isBound(self):
        return self.getThreadBinding() is not None \
                or not isinstance(self.root.get(), Unbound)

    def set(self, val):
        self.validate(self.getValidator(), val)
        b = self.getThreadBinding()
        if b is not None:
            if currentThread() != b.thread:
                raise IllegalStateException(
                    "Can't set!: {0} from non-binding thread".format(self.sym))
            b.val = val
            return self

        raise IllegalStateException(
            "Can't change/establish root binding of: {0} with set".
            format(self.sym))
        
    def alterRoot(self, fn, args):
        return self.root.mutate(lambda old: fn(old, *(args if args else ())))

    def hasRoot(self):
        return not isinstance(self.root.get(), Unbound)

    def bindRoot(self, root):
        self.validate(self.getValidator(), root)
        self.root.set(root)
        return self

    def __call__(self, *args, **kw):
        """Exists for Python interop, don't use in clojure code"""
        return self.deref()(*args, **kw)

    def deref(self):
        b = self.getThreadBinding()
        if b is not None:
            return b.val
        return self.root.get()

    def getThreadBinding(self):
        if self.threadBound:
            e = dvals.get(Frame).bindings.entryAt(self)
            if e is not None:
                return e.getValue()
        return None

    def setMeta(self, meta):
        self._meta = meta
        if self._meta and self._meta[STATIC_KEY]:
            self.setDynamic(False)
        return self

    def setMacro(self):
        self.alterMeta(lambda x, y, z: x.assoc(y, z), macrokey, True)

    def writeAsString(self, writer):
        writer.write(repr(self))

    def writeAsReplString(self, writer):
        writer.write(repr(self))

    def __repr__(self):
        if self.ns is not None:
            return "#'{0}/{1}".format(self.ns.__name__, self.sym)
        return "#<Var: {0}>".format(self.sym or "--unnamed--")


class TBox(object):
    def __init__(self, thread, val):
        self.thread = thread
        self.val = val


class Unbound(IFn):
    def __init__(self, v):
        self.v = v

    def __repr__(self):
        return "Unbound {0}".format(self.v)

    def __call__(self, *args, **kwargs):
        raise ArityException(
            "Attempting to call unbound fn: {0}".format(self.v))


class Frame(object):
    def __init__(self, bindings=EMPTY, prev=None):
        self.bindings = bindings
        self.prev = prev

    def clone(self):
        return Frame(self.bindings)


def pushThreadBindings(bindings):
    f = dvals.get(Frame)
    bmap = f.bindings
    for v in bindings:
        value = bindings[v]
        if not v.dynamic:
            raise IllegalStateException(
                "Can't dynamically bind non-dynamic var: {0}/{1}".
                format(v.ns, v.sym))
        v.validate(v.getValidator(), value)
        v.threadBound = True
        bmap = bmap.assoc(v, TBox(currentThread(), value))
    dvals.set(Frame(bmap, f))


def popThreadBindings():
    f = dvals.get(Frame)
    if f.prev is None:
        raise IllegalStateException("Pop without matching push")
    dvals.set(f.prev)


@contextlib.contextmanager
def threadBindings(bindings):
    pushThreadBindings(bindings)
    try:
        yield
    finally:
        popThreadBindings()


def getThreadBindingFrame():
    f = dvals.get(Frame)
    return f


def cloneThreadBindingFrame():
    f = dvals.get(Frame).clone()
    return f


def resetThreadBindingFrame(val):
    dvals.set(val)

