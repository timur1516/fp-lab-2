(ns fp-lab-2.unit-tests
  (:require
   [clojure.test :refer [deftest testing is run-tests]]
   [fp-lab-2.core :as dict]))

(deftest test-dict-basic
  (testing "Базовые операции insert/get/delete"
    (let [d (dict/empty-dict)]
      (is (nil? (dict/get-value d :a)))
      (let [d1 (dict/insert d :a 1)]
        (is (= 1 (dict/get-value d1 :a)))
        (let [d2 (dict/delete d1 :a)]
          (is (nil? (dict/get-value d2 :a))))))))

(deftest test-insert-all
  (testing "Вставка массива пар"
    (let [d (-> (dict/empty-dict)
                (dict/insert-all [[:a 1] [:b 2] [:c 3]]))]
      (is (= 1 (dict/get-value d :a)))
      (is (= 2 (dict/get-value d :b)))
      (is (= 3 (dict/get-value d :c))))))

(deftest test-get-keys-values-pairs
  (testing "Получение ключей, значений, пар"
    (let [d (-> (dict/empty-dict)
                (dict/insert :a 1)
                (dict/insert :b 2))]
      (is (= #{:a :b} (set (dict/get-keys d))))
      (is (= #{1 2} (set (dict/get-values d))))
      (is (= #{[:a 1] [:b 2]} (set (dict/get-pairs d)))))))

(deftest test-delete
  (testing "Удаление по существующему ключу"
    (let [d (-> (dict/empty-dict)
                (dict/insert :a 1)
                (dict/delete :a))]
      (is (nil? (dict/get-value d :a)))))
  (testing "Удаление по не существующему ключу"
    (let [d (-> (dict/empty-dict)
                (dict/delete :a))]
      (is (nil? (dict/get-value d :a))))))

(deftest test-filter-dict
  (testing "Фильтрация"
    (let [d (-> (dict/empty-dict)
                (dict/insert :a 1)
                (dict/insert :b 2)
                (dict/insert :c 3)
                (dict/filter-dict (fn [[k v]] (> v 1))))]
      (is (nil? (dict/get-value d :a)))
      (is (= 2 (dict/get-value d :b)))
      (is (= 3 (dict/get-value d :c))))))

(deftest test-map-dict
  (testing "Мап"
    (let [d (-> (dict/empty-dict)
                (dict/insert :a 1)
                (dict/insert :b 2)
                (dict/map-dict (fn [[k v]] [k (* v 10)])))]
      (is (= 10 (dict/get-value d :a)))
      (is (= 20 (dict/get-value d :b))))))

(deftest test-reduce
  (testing "Левая свёртка"
    (let [d (-> (dict/empty-dict)
                (dict/insert :a 1)
                (dict/insert :b 2)
                (dict/insert :c 3)
                (dict/reduce-left (fn [a [k v]] (+ a v)) 0))]
      (is (= 6 d))))
  (testing "Правая свёртка"
    (let [d (-> (dict/empty-dict)
                (dict/insert :a 1)
                (dict/insert :b 2)
                (dict/insert :c 3)
                (dict/reduce-right (fn [a [k v]] (+ a v)) 0))]
      (is (= 6 d)))))

(deftest test-equals-dict?
  (testing "Равные словари"
    (let [d1 (-> (dict/empty-dict)
                 (dict/insert :a 1)
                 (dict/insert :b 2))
          d2 (-> (dict/empty-dict)
                 (dict/insert :b 2)
                 (dict/insert :a 1))]
      (is (dict/equals-dict? d1 d2))))

  (testing "Не равные словари"
    (let [d1 (-> (dict/empty-dict)
                 (dict/insert :a 1)
                 (dict/insert :b 2))
          d2 (-> (dict/empty-dict)
                 (dict/insert :b 2)
                 (dict/insert :a 1))]
      (is (dict/equals-dict? d1 d2)))))

(deftest test-merge-dict
  (testing "Слияние словарей"
    (let [d1 (-> (dict/empty-dict)
                 (dict/insert :a 1))
          d2 (-> (dict/empty-dict)
                 (dict/insert :b 2))
          merged (dict/merge-dict d1 d2)]
      (is (= 1 (dict/get-value merged :a)))
      (is (= 2 (dict/get-value merged :b))))))

(deftest test-empty-dict
  (testing "Пустой словарь"
    (let [d (dict/empty-dict)]
      (is (nil? (dict/get-value d :nonexistent)))
      (is (empty? (dict/get-keys d)))
      (is (empty? (dict/get-values d)))
      (is (empty? (dict/get-pairs d))))))

(deftest test-monoid-identity
  (testing "Единичный элемент в моноиде"
    (let [d (-> (dict/empty-dict)
                (dict/insert :a 1)
                (dict/insert :b 2))]
      (is (dict/equals-dict? d (dict/merge-dict (dict/empty-dict) d)))
      (is (dict/equals-dict? d (dict/merge-dict d (dict/empty-dict)))))))

(deftest test-monoid-associativity
  (testing "Ассоциативность слияния"
    (let [d1 (-> (dict/empty-dict) (dict/insert :a 1))
          d2 (-> (dict/empty-dict) (dict/insert :b 2))
          d3 (-> (dict/empty-dict) (dict/insert :c 3))]
      (is (dict/equals-dict?
           (dict/merge-dict (dict/merge-dict d1 d2) d3)
           (dict/merge-dict d1 (dict/merge-dict d2 d3)))))))

(run-tests)