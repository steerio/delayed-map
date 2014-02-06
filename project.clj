(defproject delayed-map "1.0.0"
  :description "Delayed Map"
  :url "http://github.com/steerio/delayed-map"
  :java-source-paths ["java"]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repl-options {:init (require '[delayed-map.core :refer [delayed-map]])}
  :dependencies [[org.clojure/clojure "1.5.1"]])
