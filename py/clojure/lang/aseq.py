from py.clojure.lang.obj import Obj
from py.clojure.lang.cljexceptions import AbstractMethodCall
from py.clojure.lang.iseq import ISeq
from py.clojure.lang.sequential import Sequential
from py.clojure.lang.counted import Counted
from py.clojure.lang.ihasheq import IHashEq
from py.clojure.lang.iterable import Iterable
import py.clojure.lang.rt as RT

class ASeq(Obj, Sequential, ISeq, IHashEq, Iterable):
    def __eq__(self, other):
        if self is other:
            return True
        if not isinstance(other, (Sequential, list, tuple)):
            return False
        se = RT.seq(other)
        ms = self.seq()
        while se is not None:
            if ms is None or not se.first() == ms.first():
                return False
            ms = ms.next()
            se = se.next()	
        return ms is None

    def __hash__(self):
        if not hasattr(self, "_hash") or self._hash == -1:
            hval = -1
            for s in self:
                curhash = 0
                if s.first() is not None:
                    curhash = hash(s.first())
                hval = 31 * hval + curhash
            self._hash = hval
        return self._hash

    def seq(self):
        return self

    def count(self):
        i = 1
        for s in self.interator():
            if isinstance(s, Counted):
                return i + s.count()
            i += 1
        return i

    def more(self):
        s = self.next()
        if s is None:
            from py.clojure.lang.persistentlist import EMPTY
            return EMPTY
        return s

    def first(self):
        raise AbstractMethodCall(self)

    def __iter__(self):
        s = self.seq()
        while s is not None:
            yield s
            s = s.next()

    def hasheq(self):
        hash = 1
        for s in self:
            hash = 31 * hash + Util.hasheq(s.first())
        return hash
