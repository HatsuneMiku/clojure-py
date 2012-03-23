from clojure.lang.ipersistentvector import IPersistentVector
from clojure.lang.cljexceptions import AbstractMethodCall, ArityException
from clojure.lang.indexableseq import IndexableSeq
import clojure.lang.rt as RT
from clojure.lang.iprintable import IPrintable
from clojure.lang.ipersistentset import IPersistentSet

class APersistentVector(IPersistentVector, IPrintable):
    def __iter__(self):
        for x in range(len(self)):
            yield self.nth(x)

    def peek(self):
        if len(self):
            return self.nth(len(self) - 1)
        return None

    def __getitem__(self, item):
        return self.nth(item)

    def seq(self):
        if not len(self):
            return None
        return IndexableSeq(self, 0)
        
    def __eq__(self, other):
        s = self.seq()
        if not RT.isSeqable(other) or isinstance(other, IPersistentSet):
            return False
        o = RT.seq(other)
        return s == o
    
    def __hash__(self):
        s = self.seq()
        if not s is None:
            return s.hasheq();
        else:
            return 0

    def __ne__(self, other):
        return not self == other

    # $$$:
    # Placing these print methods here will cover:
    # MapEntry, PersistentVector, and SubVec
    #
    # But I'm calling seq().
    #
    # The commented out loop below does not have to call seq, but it will not
    # work for map entry (no nth() error). I'm guessing seq() isn't as
    # efficient as the second method. At this point I'm just trying to get a
    # complete IPrintable system *working*.
    def writeAsString(self, writer):
        writer.write("[")
        s = self.seq()
        while s is not None:
            RT.protocols.writeAsString(s.first(), writer)
            if s.next() is not None:
                writer.write(" ")
            s = s.next()
        writer.write("]")
        # n = len(self) - 1
        # writer.write("[")
        # for i, item in enumerate(self):
        #     RT.protocols.writeAsString(item, writer)
        #     if i < n:
        #         writer.write(" ")
        # writer.write("]")

    def writeAsReplString(self, writer):
        writer.write("[")
        s = self.seq()
        while s is not None:
            RT.protocols.writeAsReplString(s.first(), writer)
            if s.next() is not None:
                writer.write(" ")
            s = s.next()
        writer.write("]")


class SubVec(APersistentVector):
    def __init__(self, meta, v, start, end):
        self._meta = meta
        if isinstance(v, SubVec):
            start += v.start
            end += v.start
            v = v.v
        self.v = v
        self.start = start
        self.end = end

    def nth(self, i):
        if self.start + i >= self.end:
            raise Exception("Index out of range")
        return self.v.nth(self.start + i)

    def assocN(self, i, val):
        if self.start + i > self.end:
            raise Exception("Index out of range")
        elif self.start + i == self.end:
            return self.cons(val)
        return SubVec(self._meta,
                      self.v.assocN(self.start + self.i, val),
                      self.start,
                      self.end)

    def __len__(self):
        return self.end - self.start

    def cons(self, o):
        return SubVec(self._meta,
                      self.v.assocN(self.end, o),
                      self.start,
                      self.end + 1)

    def empty(self):
        from clojure.lang.persistentvector import EMPTY as EMPTY_VECTOR
        return EMPTY_VECTOR.withMeta(self.meta())

    def pop(self):
        from clojure.lang.persistentvector import EMPTY as EMPTY_VECTOR
        if self.end - 1 == self.start:
            return EMPTY_VECTOR
        return SubVec(self._meta, self.v, self.start, self.end - 1)

    def withMeta(self, meta):
        if self.meta == meta:
            return self
        return SubVec(self._meta, self.v, self.start, self.end)

    def meta(self):
        return self.meta()
