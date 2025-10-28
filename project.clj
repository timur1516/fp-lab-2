(defproject fp-lab-2 "1.0"
  :description "Написание структуры данных на Clojure"
  :url "https://github.com/timur1516/fp-lab-2"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [org.clojure/test.check "1.1.0"]]
  :target-path "target/%s"
  :plugins [[dev.weavejester/lein-cljfmt "0.13.1"],
            [jonase/eastwood "1.4.3"],
            [com.github.clj-kondo/lein-clj-kondo "0.2.5"]]
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :test {:injections [(require 'fp-lab-2.pretty-reporter) 
                                 (require 'fp-lab-2.assertions)]}})
