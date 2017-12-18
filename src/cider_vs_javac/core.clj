(ns cider-vs-javac.core
  (:require [virgil.compile :refer [compile-all-java]]))

(defn doit
  []
  (println "Compiling java-src")
  (compile-all-java ["java-src"])
  (println "Compiling java-src-with-errors")
  (compile-all-java ["java-src-with-errors"]))
