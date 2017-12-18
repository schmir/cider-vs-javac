(defproject cider-vs-javac "0.1.0-SNAPSHOT"
  :description "demonstrate problems running javac with cider-nrepl-0.16.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [virgil "0.1.7"]]
  :repl-options {:init-ns cider-vs-javac.core}
  :plugins [[lein-virgil "0.1.7"]
            [cider/cider-nrepl "0.16.0-SNAPSHOT"]]
  ;; :injections [(println "INJECT: require cider.nrepl.middleware.util.java")
  ;;              (require 'cider.nrepl.middleware.util.java)]
  :java-source-paths ["java-src"])
