(ns fp-lab-2.core
  (:gen-class))

(def N_BUCKETS 16)

(defprotocol Dict
  (insert [dict key value])
  (insert-all [dict pairs])
  (get-value [dict  key])
  (contais-key? [dict key])
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

(defn my-reduce-left [f init coll]
  (if (empty? coll)
    init
    (my-reduce-left f (f init (first coll)) (rest coll))))

(defn my-reduce-right [f init coll]
  (if (empty? coll)
    init
    (f (my-reduce-right f init (rest coll)) (first coll))))

(defn my-map [f coll]
  (if (empty? coll)
    coll
    (cons (f (first coll)) (my-map f (rest coll)))))

(defn my-filter [pred coll]
  (cond
    (empty? coll) coll
    (pred (first coll)) (cons (first coll) (my-filter pred (rest coll)))
    :else (my-filter pred (rest coll))))

(defrecord SCDict [buckets]
  Dict

  (insert [dict key value]
    (let [idx (mod (hash key) (count (:buckets dict)))
          bucket (nth (:buckets dict) idx)
          new-bucket (conj (remove #(= (first %) key) bucket) [key value])]
      (assoc dict :buckets (assoc (:buckets dict) idx new-bucket))))

  (insert-all [dict pairs]
    (my-reduce-left (fn ins [d [k v]] (insert d k v)) dict pairs))

  (get-value [dict key]
    (let [idx (mod (hash key) (count (:buckets dict)))
          bucket (nth (:buckets dict) idx)
          entry (some #(when (= (first %) key) %) bucket)]
      (when entry (second entry))))

  (contais-key? [dict key]
    (let [idx (mod (hash key) (count (:buckets dict)))
          bucket (nth (:buckets dict) idx)
          entry (some #(when (= (first %) key) %) bucket)]
      (boolean entry)))

  (get-keys [dict]
    (my-map first (get-pairs dict)))

  (get-values [dict]
    (my-map second (get-pairs dict)))

  (get-pairs [dict]
    (my-reduce-left concat () (:buckets dict)))

  (delete [dict key]
    (let [idx (mod (hash key) (count (:buckets dict)))
          bucket (nth (:buckets dict) idx)
          new-bucket (remove #(= (first %) key) bucket)]
      (assoc dict :buckets (assoc (:buckets dict) idx new-bucket))))

  (filter-dict [dict pred]
    (->> (get-pairs dict)
         (my-filter pred)
         (insert-all (->SCDict (vec (repeat N_BUCKETS []))))))

  (map-dict [dict f]
    (->> (get-pairs dict)
         (my-map f)
         (insert-all (->SCDict (vec (repeat N_BUCKETS []))))))

  (reduce-left [dict f init]
    (->> (get-pairs dict)
         (my-reduce-left f init)))

  (reduce-right [dict f init]
    (->> (get-pairs dict)
         (my-reduce-right f init)))

  (equals-dict? [dict1 dict2]
    (let [p1 (get-pairs dict1)
          p2 (get-pairs dict2)]
      (and
       (my-reduce-left (fn f [acc [k v]] (and acc (= (get-value dict1 k) v))) true p2)
       (my-reduce-left (fn f [acc [k v]] (and acc (= (get-value dict2 k) v))) true p1))))

  (merge-dict [dict1 dict2]
    (->> (get-pairs dict2)
         (insert-all dict1))))

(defn empty-dict []
  (->SCDict (vec (repeat N_BUCKETS ()))))

(defn dict-from [pairs]
  (-> (empty-dict)
      (insert-all pairs)))