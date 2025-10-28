(ns fp-lab-2.core
  (:gen-class))

(def N_BUCKETS 16)

(defprotocol Dict
  (insert [dict key value])
  (insert-all [dict pairs])
  (get-value [dict  key])
  (contains-key? [dict key])
  (get-keys [dict])
  (get-values [dict])
  (get-pairs [dict])
  (delete [dict  key])
  (filter-dict [dict pred])
  (map-dict [dict f])
  (reduce-left [dict f init])
  (reduce-right [dict f init])
  (equals-dict? [dict1 dict2])
  (merge-dict [dict1 dict2]))

(defn my-reduce-right [f init coll]
  (if (empty? coll)
    init
    (f (my-reduce-right f init (rest coll)) (first coll))))

(deftype SCDict [buckets]
  Dict

  (insert [dict key value]
    (let [idx (mod (hash key) (count buckets))
          bucket (nth buckets idx)
          new-bucket (conj (remove #(= (first %) key) bucket) [key value])]
      (SCDict. (assoc buckets idx new-bucket))))

  (insert-all [dict pairs]
    (reduce (fn ins [d [k v]] (insert d k v)) dict pairs))

  (get-value [dict key]
    (let [idx (mod (hash key) (count buckets))
          bucket (nth buckets idx)
          entry (some #(when (= (first %) key) %) bucket)]
      (when entry (second entry))))

  (contains-key? [dict key]
    (let [idx (mod (hash key) (count buckets))
          bucket (nth buckets idx)
          entry (some #(when (= (first %) key) %) bucket)]
      (boolean entry)))

  (get-keys [dict]
    (map first (get-pairs dict)))

  (get-values [dict]
    (map second (get-pairs dict)))

  (get-pairs [dict]
    (reduce concat () buckets))

  (delete [dict key]
    (let [idx (mod (hash key) (count buckets))
          bucket (nth buckets idx)
          new-bucket (remove #(= (first %) key) bucket)]
      (SCDict. (assoc buckets idx new-bucket))))

  (filter-dict [dict pred]
    (->> (get-pairs dict)
         (filter pred)
         (insert-all (SCDict. (vec (repeat N_BUCKETS []))))))

  (map-dict [dict f]
    (->> (get-pairs dict)
         (map f)
         (insert-all (SCDict. (vec (repeat N_BUCKETS []))))))

  (reduce-left [dict f init]
    (->> (get-pairs dict)
         (reduce f init)))

  (reduce-right [dict f init]
    (->> (get-pairs dict)
         (my-reduce-right f init)))

  (equals-dict? [dict1 dict2]
    (let [p1 (get-pairs dict1)
          p2 (get-pairs dict2)]
      (and
       (reduce (fn f [acc [k v]] (and acc (= (get-value dict1 k) v))) true p2)
       (reduce (fn f [acc [k v]] (and acc (= (get-value dict2 k) v))) true p1))))

  (merge-dict [dict1 dict2]
    (->> (get-pairs dict2)
         (insert-all dict1)))

  clojure.lang.ILookup
  (valAt [dict k] (get-value dict k))
  (valAt [dict k not-found]
    (let [v (get-value dict k)] (if (nil? v) not-found v)))

  clojure.lang.Associative
  (assoc [dict k v] (insert dict k v))
  (containsKey [dict k] (contains-key? dict k))
  (entryAt [dict k]
    (when-let [v (get-value dict k)]
      (clojure.lang.MapEntry. k v)))

  clojure.lang.IPersistentMap
  (without [dict k] (delete dict k))

  clojure.lang.Seqable
  (seq [dict]
    (seq (map (fn [[k v]] (clojure.lang.MapEntry. k v))
              (get-pairs dict))))

  clojure.lang.Counted
  (count [dict]
    (reduce + 0 (map count buckets)))

  clojure.lang.IPersistentCollection
  (cons [dict o]
    (cond
      (instance? clojure.lang.IMapEntry o) (insert dict (key o) (val o))
      (and (vector? o) (= 2 (count o)))    (insert dict (o 0) (o 1))
      (map? o)                             (insert-all dict o)
      (sequential? o)                      (insert-all dict o)
      :else (throw (ex-info "Unsupported element for conj/cons on SCDict" {:value o}))))
  (empty [dict] ((SCDict. (vec (repeat N_BUCKETS [])))))
  (equiv [dict other]
    (cond
      (instance? SCDict other) (equals-dict? dict other)
      (map? other) (= (into {} (seq dict)) other)
      :else false)))

(defn empty-dict []
  (SCDict. (vec (repeat N_BUCKETS []))))

(defn dict-from [pairs]
  (-> (empty-dict)
      (insert-all pairs)))