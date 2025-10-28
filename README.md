# Лабораторная работа №2

---

Выполнил: Ступин Тимур Русланович

Группа: P3308

Преподаватель: Пенской Александр Владимирович

Вариант: `sc-dict`

---

## Требования

1. Функции:

    - добавление и удаление элементов;

    - фильтрация;

    - отображение (map);

    - свертки (левая и правая);

    - структура должна быть моноидом.

2. Структуры данных должны быть неизменяемыми.

3. Библиотека должна быть протестирована в рамках unit testing.

4. Библиотека должна быть протестирована в рамках property-based тестирования (как минимум 3 свойства, включая свойства моноида).

5. Структура должна быть полиморфной.

6. Требуется использовать идиоматичный для технологии стиль программирования. Примечание: некоторые языки позволяют получить большую часть API через реализацию небольшого интерфейса. Так как лабораторная работа про ФП, а не про экосистему языка -- необходимо реализовать их вручную и по возможности -- обеспечить совместимость.

7. Обратите внимание:

    - API должно быть реализовано для заданного интерфейса и оно не должно "протекать". На уровне тестов -- в первую очередь нужно протестировать именно API (dict, set, bag).

    - Должна быть эффективная реализация функции сравнения (не наивное приведение к спискам, их сортировка с последующим сравнением), реализованная на уровне API, а не внутреннего представления.

## Реализация

Принцип работы Separate Chaining Hashmap достаточно просто. Заведём вектор фиксированного размера `N_BUCKETS`. Каждый элемент вектора будет связным списком. Таким образом связный список с индексом `i` будет содержать в себе все элементы, значение хэш функции от которых равно `i`. Так как значение хэш функции может быть больше `N_BUCKETS` то будем при вычислении хэша брать остаток от деления на `N_BUCKETS`. Для того чтобы иметь возможность работать с ключами будем хранить в списках не значения, а пары {`ключ`, `значение`}.

Для того чтобы корректно реализовать интерфейс, я использовал конструкцию `defprotocol`, которая при компиляции как раз таки преобразуется в обычный java интерфейс. Для данной задачи я получил интерфейс такого вида:

```clj
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
```

Далее я реализовал этот интерфейс при помощи конструкции `defrecord`.

```clj
(defrecord SCDict [buckets]
  Dict

  (insert [dict key value]
    (let [idx (mod (hash key) (count (:buckets dict)))
          bucket (nth (:buckets dict) idx)
          new-bucket (conj (remove #(= (first %) key) bucket) [key value])]
      (assoc dict :buckets (assoc (:buckets dict) idx new-bucket))))

  (insert-all [dict pairs]
    (my-reduce-left (fn ins [d [k v]] (insert d k v)) dict pairs))

  ...
```

### Основные функции

#### Добавление элемента

```clj
(insert [dict key value]
(let [idx (mod (hash key) (count (:buckets dict)))
        bucket (nth (:buckets dict) idx)
        new-bucket (conj (remove #(= (first %) key) bucket) [key value])]
    (assoc dict :buckets (assoc (:buckets dict) idx new-bucket))))
```

#### Удаление элемента

```clj
(delete [dict key]
(let [idx (mod (hash key) (count (:buckets dict)))
        bucket (nth (:buckets dict) idx)
        new-bucket (remove #(= (first %) key) bucket)]
    (assoc dict :buckets (assoc (:buckets dict) idx new-bucket))))
```

#### Получение элемента по ключу

```clj
(get-value [dict key]
(let [idx (mod (hash key) (count (:buckets dict)))
        bucket (nth (:buckets dict) idx)
        entry (some #(when (= (first %) key) %) bucket)]
    (when entry (second entry))))
```

#### Фильтрация

Реализована по функции pred, которая должна принимать пару `[key value]`.

```clj
(filter-dict [dict pred]
(->> (get-pairs dict)
        (my-filter pred)
        (insert-all (->SCDict (vec (repeat N_BUCKETS []))))))
```

#### Отображение

Функция `f` должна принимать пару `[key value]` и возвращать пару `[key value]`.

```clj
(map-dict [dict f]
(->> (get-pairs dict)
        (my-map f)
        (insert-all (->SCDict (vec (repeat N_BUCKETS []))))))
```

#### Свёртки

Функция `f` должна принимать тройку `[a [key value]]`, где `a` - накапливаемое значение.

```clj
(reduce-left [dict f init]
(->> (get-pairs dict)
        (my-reduce-left f init)))

(reduce-right [dict f init]
(->> (get-pairs dict)
        (my-reduce-right f init)))
```

### Свойства реализованной структуры данных

- Неизменяемость обеспечивается благодаря тому что любая функция изменения словаря возвращает новый словарь

- Свойства моноида выполняются, так как присутствует понятие единичного элемента - пустой словарь и понятие слияния двух словарей благодаря функции `merge-dict`

Полный код реализации приведён в файле [core.clj](src/fp_lab_2/core.clj)

Фактически реализованная структура содержит только одно поле - `buckets` - вектор списков элементов.

Помимо основных функций интерфейса, я также реализовал функции для создания пустого словаря и словаря на основе списка пар

```clj
(defn empty-dict []
  (->SCDict (vec (repeat N_BUCKETS ()))))

(defn dict-from [pairs]
  (-> (empty-dict)
      (insert-all pairs)))
```

Также я написал свою реализацию функции правой свёртки, так дефолтная реализация reduce в clojure поддерживает только левую.

```clj
(defn my-reduce-right [f init coll]
  (if (empty? coll)
    init
    (f (my-reduce-right f init (rest coll)) (first coll))))
```

## Тестирование

Для проверки корректности работы реализованной структуры данных я написал unit тесты с использованием `clojure.test` а
также property-based тесты с использованием `org.clojure/test.check`.

Полный код тестов приведён в файлах [unit_tests](test/fp_lab_2/unit_tests.clj) и [property_tests](test/fp_lab_2/property_tests.clj)

## Дополнительное задание

> Реализовать стандартный интерфейс коллекции  
> Добавить кастомный вывод для `(is (dict/equals-dict?...))`

### Интерфейс коллекций

Для реализации стандартного интерфейса коллекции я доопределил следующие интерфейсы:

- `clojure.lang.ILookup`:

    - `get`: получить элемент по ключу

- `clojure.lang.Associative`:
  
    - `assoc`: добавить элемент
  
    - `containsKey`: проверить наличие ключа
  
    - `entryAt`: полчить выхождение по ключу

- `clojure.lang.IPersistentMap`:

    - `without`: удалить элемент по ключу

- `clojure.lang.Seqable`:

    - `seq`: получить последовательность `IMapEntry`

- `clojure.lang.Counted`:

    - `count`: получить количество элементов

- `clojure.lang.IPersistentCollection`:

    - `cons`: добавить элемент
  
    - `empty`: получить пустую коллекцию
  
    - `equiv`: проверить равенство коллекций

Я также добавил тесты для проверки корректности определённых интерфейсов

### Кастомный вывод тестов

Для этого я переопределил обработку исключения для `dict/equals-dict?` таки образом чтобы при несоотвествии вычислялись следуюшие списки:

- `only-in-1`: вхождения которые есть только в первом словаре

- `only-in-2`: выхождения которые есть только во втором словаре

- `mismatched`: словарь вида `k: [v1 v2]` отражающий что ключ `k` есть в обоих словарях но значения по нему разлличны

Для получения этих данных я написал функцию `dict-diff`

Все эти функции содержатся в файле [assertions.clj](test/fp_lab_2/assertions.clj)

Также я добавил кастомный вывод для FAIL при тесте, для того чтобы выводить дополнительные поля

Для этого я переопределил `report` при `fail` используя `defmethod`

Исходный код размещён в файле [pretty_reporter.clj](test/fp_lab_2/pretty_reporter.clj)

Для проверки правильности работы я специально внёс неверные провеки в тест со сравнением словарей:

```clj
(testing "Равные словари"
  (let [d1 (-> (dict/empty-dict)
                (dict/insert :a 1)
                (dict/insert :b 2)
                (dict/insert :c 5))
        d2 (-> (dict/empty-dict)
                (dict/insert :b 3)
                (dict/insert :a 1)
                (dict/insert :d 15))]
    (is (dict/equals-dict? d1 d2))))
```

И при запуске получил такой репорт:

```bash
FAIL: 
  expected: (dict/equals-dict? d1 d2)
  only-in-1: {:b 2, :c 5}
  only-in-2: {:d 15, :b 3}
  mismatched (k -> [v1 v2]): {:b [2 3]}
```

что соотвествует ожиданиям.

## Вывод

В ходе выполнения работы я научился создавать свои неизменяемые структуры данных в функциональном стиле Clojure. Также я освоил
новые подходы к тестирования такие как property-based тесты.
