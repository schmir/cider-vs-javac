(defproject cider-vs-javac "0.1.0-SNAPSHOT"
  :description "demonstrate problems running javac with cider-nrepl-0.16.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [cider/cider-nrepl "0.16.0-SNAPSHOT"]]
  :repl-options {:init-ns cider-vs-javac.core}
  ;;:resource-paths ["/usr/lib/jvm/oracle-java8-jdk-amd64/lib/tools.jar"]
  )
