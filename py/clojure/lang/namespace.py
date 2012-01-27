from py.clojure.lang.areference import AReference
from py.clojure.lang.atomicreference import AtomicReference
from py.clojure.lang.persistenthashmap import EMPTY as EMPTY_MAP
from py.clojure.lang.cljexceptions import InvalidArgumentException, IllegalStateException, ArityException
import py.clojure.lang.rt as RT
import sys, new

namespaces = AtomicReference(EMPTY_MAP)

def areDifferentInstancesOfSameClassName(o1, o2):
    return o1.__class__ is o2.__class__

def addDefaultImports(mod):
    import py.clojure.lang.rt as RT
    import py.clojure.standardimports as stdimps
    for i in dir(stdimps):
        if i.startswith("_"):
            continue
        setattr(mod, i, getattr(stdimps, i))
    if "clojure" in sys.modules:
        core = sys.modules["clojure"].core
        setattr(getattr(mod, "clojure"), "core", core)
        for i in dir(core):
            if i.startswith("_"):
                continue
            setattr(mod, i, getattr(core, i))
    return mod

def findOrCreateIn(module, parts):
    if not parts:
        return module
    part = parts[0]
    parts = parts[1:]
    if hasattr(module, part):
        return findOrCreateIn(getattr(module, part), parts)
    mod = new.module(module.__name__ + "." + part)
    setattr(module, part, mod)
    return findOrCreateIn(mod, parts)

def findOrCreate(name):
    from py.clojure.lang.symbol import Symbol
    if isinstance(name, Symbol) and name.name is not None:
        name = name.name
    parts = name.split(".")

    if parts[0] in sys.modules:
        mod = sys.modules[parts[0]]
    else:
        mod = new.module(parts[0])
        sys.modules[parts[0]] = mod

    mod = findOrCreateIn(mod, parts[1:])
    addDefaultImports(mod)

    return mod


def remove(name):
    if name.equals(RT.CLOJURE_NS.name):
        raise IllegalArgumentException("Cannot remove clojure namespace");

    while name in namespaces.get():
        newns = namespaces.get().without(name)
        namespaces.compareAndSet(namespaces, newns)

def find(name):
    return namespaces.get()[name]

def findItem(ns, sym):
    from py.clojure.lang.symbol import Symbol
    if isinstance(sym, Symbol):
        if sym.ns == ns.__name__:
            if not hasattr(ns, sym.name):
                return None
            return getattr(ns, sym.name)
        if sym.ns is not None:
            mod = findModule(sym.ns)
            if hasattr(mod, sym.name):
                return getattr(mod, sym.name)
            return None
        if not hasattr(ns, str(sym)):
            return None
        return getattr(ns, str(sym))
    return getattr(ns, sym)

def findModule(sym, module = None):
    if module is None:
        sym = sym.split(".")
        parts = sym[1:]
        name = sym[0]
        if name not in sys.modules:
            return None
        if len(parts):
            return findModule(parts, sys.modules[name])
        return sys.modules[name]

    name = sym[0]
    parts = sym[1:]
    if not hasattr(module, name):
        return None
    if len(parts):
        return findModule(parts, getattr(module, name))
    return getattr(module, name)


def intern(ns, sym):
    from py.clojure.lang.var import Var

    if sym.ns is not None:
        raise InvalidArgumentException("Can't intern namespace-qualified symbol")
    map = ns
    v = Var(ns, sym)
    setattr(ns, sym.name, v)

    return v

#
#class Namespace(AReference):
#    def __init__(self, name):
#        self._meta = name.meta()
#        self.name = name
#        self.mappings = AtomicReference(RT.DEFAULT_IMPORTS)
#        self.aliases = AtomicReference(RT.map())
#
#    @staticmethod
#    def all():
#        return RT.seq(namespaces.get().values())
#
#    def getName(self):
#        return self.name
#
#    def getMappings(self):
#        return self.mappings
#
#    def intern(self, sym):
#        from py.clojure.lang.var import Var
#
#        if sym.ns is not None:
#            raise InvalidArgumentException("Can't intern namespace-qualified symbol")
#        map = self.getMappings().get()
#        v = None
#
#        o = map[sym] if map is not None else None
#        v = None
#        while o is None:
#            if v is None:
#                v = Var(self, sym)
#            newmap = map.assoc(sym, v)
#            self.mappings.compareAndSet(map, newmap)
#            map = self.getMappings().get()
#            o = map[sym]
#
#        if isinstance(o, Var) and o.ns is self:
#            return o
#
#        if v is None:
#            v = Var(self, sym)
#
#        self.warnOrFailOnReplace(sym, o, v)
#
#        while not self.mappings.compareAndSet(map, map.assoc(sym, v)):
#            map = self.getMappings()
#
#        return v
#
#    def wardOrFailOnReplace(self, sym, o, v):
#        from py.clojure.lang.var import Var
#        from py.clojure.lang.rt import CLOJURE_NS
#        if isinstance(o, Var):
#            ns = o.ns
#            if ns is self:
#               return
#            if ns is not CLOJURE_NS:
#                raise IllegalStateException(sym + " already refers to: " + o + " in namespace: " + self.name)
#        RT.errPrintWriter().println("WARNING: " + sym + " already refers to: " + o + " in namespace: " + self.name
#                                    + ", being replaced by: " + v)
#
#
#    def reference(self, sym, val):
#        if sym.ns is not None:
#            raise InvalidArgumentException("Can't intern namespace-qualified symbol")
#
#        map = self.getMappings().get()
#        o = map[sym]
#        while o is None:
#            newMap = map.assoc(sym, val)
#            self.mappings.compareAndSet(map, newMap)
#            map = self.getMappings()
#
#        if o is val:
#            return o
#
#        self.warnOrFailOnReplace(sym, o, val);
#
#        while not mappings.compareAndSet(map, map.assoc(sym, val)):
#            map = self.getMappings()
#
#        return val
#
#    def referenceClass(self, sym, val):
#        if sym.ns is not None:
#            raise InvalidArgumentException("Can't intern namespace-qualified symbol")
#        map = self.getMappings().get()
#        c = map[sym]
#
#        while c is None or areDifferentInstancesOfSameClassName(c, val):
#            newMap = map.assoc(sym, val)
#            self.mappings.compareAndSet(map, newMap)
#            map = self.getMappings().get()
#            c = map.valAt(sym)
#
#        if c is val:
#            return c
#
#        raise IllegalStateException(sym + " already refers to: " + c + " in namespace: " + self.name)
#
#
#    def unmap(self, sym):
#        if sym.ns is not None:
#            raise InvalidArgumentException("Can't unintern namespace-qualified symbol");
#        map = self.getMappings().get()
#
#        while sym in map:
#            newMap = map.without(sym)
#            self.mappings.compareAndSet(map, newMap)
#            map = self.getMappings().get()
#
#    def importClass(self, *args):
#        if len(args) == 2:
#            return self.referenceClass(args[0], args[1])
#
#        if len(args) == 1:
#            n = args[0].__name__
#            return self.importClass(Symbol.intern(n), args[0])
#        raise ArityException
#
#    def refer(self, sym, var):
#        return self.reference(sym, var)
#
#
#
#
#
#    def getMapping(self, name):
#        return mappings.get()[name]
#
#    def findInternedVar(self, symbol):
#        o = self.mappings.get().valAt(symbol)
#        if o is not None and isinstance(o, Var) and o.ns is self:
#            return o
#        return None
#
#
#    def getAliases(self):
#        return aliases.get()
#
#    def lookupAlias(self, alias):
#        return self.getAliases()[alias]
#
#    def addAlias(self, alias, ns):
#        if alias is None or ns is None:
#            raise AttributeError("Expecting Symbol + Namespace")
#        map = self.getAliases()
#        while alias not in map:
#            newMap = map.assoc(alias, ns)
#            self.aliases.compareAndSet(map, newMap)
#            map = self.getAliases()
#
#        # you can rebind an alias, but only to the initially-aliased namespace.
#        if not map.valAt(alias) == ns:
#            raise InvalidArgumentException("Alias " + alias + " already exists in namespace "
#                                        + self.name + ", aliasing " + map.valAt(alias))
#
#    def removeAlias(self, alias):
#        map = self.getAliases();
#        while alias in map:
#            newMap = map.without(alias)
#            self.aliases.compareAndSet(map, newMap)
#            map = self.getAliases()
