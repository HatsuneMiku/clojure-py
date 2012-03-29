"""pvector-test.py

Wednesday, March 28 2012
"""

import unittest

import clojure.lang.persistentvector as pv
from clojure.lang.cljexceptions import (IndexOutOfBoundsException,
                                        IllegalStateException)

uobj = object()
pseudoMetaData = object()

class TestPersistentVector(unittest.TestCase):
    # vec(), create()
    def testCreation_PASS(self):
        for k, v in testCreationMap_PASS.items():
            self.assertEqual(k, v)
    # __init__() with meta data, empty()
    def testMetaData_PASS(self):
        for k, v in testMetaDataMap_PASS.items():
            self.assertEqual(k, v)
    # __call__()
    def testCall_PASS(self):
        for k, v in testCallMap_PASS.items():
            self.assertEqual(k, v)
    def testCall_FAIL(self):
        v = pv.vec([])
        # no default argument allowed
        self.assertRaises(IndexOutOfBoundsException, v.__call__, 0)
        self.assertRaises(IndexOutOfBoundsException, v.__call__, 99)
        self.assertRaises(IndexOutOfBoundsException, v.__call__, -323)
    # nth()
    def testNth_PASS(self):
        for k, v in testNthMap_PASS.items():
            self.assertEqual(k, v)
    def testNth_FAIL(self):
        v = pv.vec([])
        # no default argument given
        self.assertRaises(IndexOutOfBoundsException, v.nth, 0)
        self.assertRaises(IndexOutOfBoundsException, v.nth, 99)
        self.assertRaises(IndexOutOfBoundsException, v.nth, -2343)
    # assocN()
    def testAssocN_PASS(self):
        for k, v in testAssocNMap_PASS.items():
            self.assertEqual(k, v)
    def testAssocN_FAIL(self):
        v = pv.vec([])
        self.assertRaises(IndexOutOfBoundsException, v.assocN, 2, uobj)
    # __len__()
    def testLen_PASS(self):
        for k, v in testLenMap_PASS.items():
            self.assertEqual(k, v)
    # cons()
    def testCons_PASS(self):
        for k, v in testConsMap_PASS.items():
            self.assertEqual(k, v)
    # pop()
    def testPop_PASS(self):
        for k, v in testPopMap_PASS.items():
            self.assertEqual(k, v)
    def testPop_FAIL(self):
        v = pv.vec([])
        self.assertRaises(IllegalStateException, v.pop)

testCreationMap_PASS = {
    # vec
    pv.vec([]): pv.EMPTY,
    pv.vec([uobj])._tail[0]: uobj,
    pv.vec([0, 0, uobj])._tail[2]: uobj,
    # create
    pv.create(): pv.EMPTY,
    pv.create(uobj)._tail[0]: uobj,
    pv.create(0, 0, uobj)._tail[2]: uobj,
    }

testMetaDataMap_PASS = {
    pv.PersistentVector(pseudoMetaData, 0, 5, pv.EMPTY_NODE, []) \
        .meta(): pseudoMetaData,
    pv.PersistentVector(pseudoMetaData, 0, 5, pv.EMPTY_NODE, []) \
        .empty().meta(): pseudoMetaData,
    }
        
testCallMap_PASS = {
    # _tail used
    pv.vec([42])(0): 42,
    pv.vec([0, 42])(1): 42,
    pv.vec([0, 0, 42])(2): 42,
    # force Node creation
    pv.vec(range(32) + [42])(32): 42,
    # larg-ish vec
    pv.vec(range(10000))(9999): 9999,
    }
    
testNthMap_PASS = {
    # in range
    pv.vec([42]).nth(0): 42,
    pv.vec([None]).nth(0): None,
    pv.vec([0, 42]).nth(1): 42,
    pv.vec([0, None]).nth(1): None,
    pv.vec([0, 0, 42]).nth(2): 42,
    pv.vec([0, 0, None]).nth(2): None,
    pv.vec(range(32) + [42]).nth(32): 42,
    pv.vec(range(32) + [None]).nth(32): None,
    # larg-ish vec
    pv.vec(range(10000)).nth(9999): 9999,
    # out of range, default value returned 
    pv.vec([0, 0, 0]).nth(3, uobj): uobj,
    # default value of None returned 
    pv.vec([0, 0, 0]).nth(3, None): None,
    }
    
testAssocNMap_PASS = {
    # modify
    pv.vec([0]).assocN(0, uobj)[0]: uobj,
    pv.vec([0]).assocN(0, None)[0]: None,
    # append
    pv.vec([]).assocN(0, uobj)[0]: uobj,
    pv.vec([]).assocN(0, None)[0]: None,
    # large-ish vec
    pv.vec(range(10000)).assocN(10000, uobj)[10000]: uobj,
    pv.vec(range(10000)).assocN(10000, None)[10000]: None,
    }

testLenMap_PASS = {
    len(pv.vec([])): 0,
    len(pv.vec([0])): 1,
    len(pv.vec([0, 0])): 2,
    len(pv.vec(range(2342))): 2342,
    }

testConsMap_PASS = {
    pv.vec([]).cons(uobj)(0): uobj,
    pv.vec([]).cons(0).cons(uobj)(1): uobj,
    pv.vec([]).cons(0).cons(None)(1): None,
    }

testPopMap_PASS = {
    pv.vec([0]).pop(): pv.EMPTY,
    len(pv.vec(range(33)).pop()): 32,
    }
