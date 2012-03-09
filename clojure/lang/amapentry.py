from clojure.lang.apersistentvector import APersistentVector
from clojure.lang.persistentvector import PersistentVector
from clojure.lang.cljexceptions import IndexOutOfBoundsException


class AMapEntry(APersistentVector):
    def nth(self, i):
        if i == 0:
            return self.getKey()
        elif i == 1:
            return self.getValue()
        else:
            raise IndexOutOfRangeException()

    def asVector(self):
        return PersistentVector(self.getKey(), self.getValue())

    def assocN(self, i, val):
        return self.asVector().assocN(i, val)

    def __len__(self):
        return 2

    def seq(self):
        return self.asVector().seq()

    def cons(self, o):
        return self.asVector().cons(o)

    def empty(self):
        return None

    def pop(self):
        return PersistentVector(self.getKey())
