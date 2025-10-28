(ns fp-lab-2.property-tests
  {:clj-kondo/config '{:lint-as {clojure.test.check.clojure-test/defspec clojure.core/def
                                 clojure.test.check.properties/for-all clojure.core/let}}}
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [fp-lab-2.core :as dict]))

(def MIN_V 1)
(def MAX_V 100)
(def N_ITERATIONS 100)
(def N_SIZE 100)

(def gen-value (gen/choose MIN_V MAX_V))
(def gen-key gen/keyword)

(def gen-pairs (gen/vector (gen/tuple gen-key gen-value) N_SIZE))

(defspec test-random-element N_ITERATIONS
  (prop/for-all [pairs gen-pairs]
                (let [d (dict/dict-from pairs)
                      [k _] (rand-nth pairs)]
                  (dict/contains-key? d k))))

(defspec test-merge N_ITERATIONS
  (prop/for-all [pairs1 gen-pairs
                 pairs2 gen-pairs]
                (let [d1 (dict/dict-from pairs1)
                      d2 (dict/dict-from pairs2)
                      merged-d (dict/merge-dict d1 d2)
                      merged-pairs (concat pairs1 pairs2)]
                  (every? (fn [[k _]] (dict/contains-key? merged-d k)) merged-pairs))))

(defspec test-monoid-identity N_ITERATIONS
  (prop/for-all [pairs gen-pairs]
                (let [d (dict/dict-from pairs)]
                  (and (dict/equals-dict? d (dict/merge-dict (dict/empty-dict) d))
                       (dict/equals-dict? d (dict/merge-dict d (dict/empty-dict)))))))

(defspec test-monoid-associativity N_ITERATIONS
  (prop/for-all [pairs1 gen-pairs
                 pairs2 gen-pairs
                 pairs3 gen-pairs]
                (let [d1 (dict/dict-from pairs1)
                      d2 (dict/dict-from pairs2)
                      d3 (dict/dict-from pairs3)]
                  (dict/equals-dict?
                   (dict/merge-dict (dict/merge-dict d1 d2) d3)
                   (dict/merge-dict d1 (dict/merge-dict d2 d3))))))

(defspec test-ilookup N_ITERATIONS
  (prop/for-all [pairs gen-pairs]
                (let [m (into {} pairs)
                      d (dict/dict-from pairs)
                      ks (into (set (keys m)) #{::missing-1 ::missing-2})]
                  (every?
                   true?
                   (for [k ks]
                     (= (get m k ::nf) (get d k ::nf)))))))

(defspec test-associative-assoc-contains-find N_ITERATIONS
  (prop/for-all [pairs gen-pairs
                 k gen-key
                 v gen-value]
                (let [m (assoc (into {} pairs) k v)
                      d (assoc (dict/dict-from pairs) k v)]
                  (and
                   (every?
                    true?
                    (for [kk (conj (keys m) ::missing)]
                      (= (contains? m kk) (contains? d kk))))
                   (let [em (find m k)
                         ed (find d k)]
                     (or (and (nil? em) (nil? ed))
                         (= (seq em) (seq ed))))))))

(defspec test-seqable-and-counted N_ITERATIONS
  (prop/for-all [pairs gen-pairs]
                (let [m (into {} pairs)
                      d (dict/dict-from pairs)]
                  (and
                   (= (set m) (set (seq d)))
                   (= (count m) (count d))))))
