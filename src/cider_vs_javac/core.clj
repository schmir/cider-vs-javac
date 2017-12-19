(ns cider-vs-javac.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [clojure.lang
            DynamicClassLoader]
           [java.io
            File
            ByteArrayOutputStream]
           [java.util.concurrent
            ConcurrentHashMap]
           [javax.tools
            DiagnosticCollector
            ForwardingJavaFileManager
            JavaFileObject$Kind
            SimpleJavaFileObject
            ToolProvider]))

;; a shout-out to https://github.com/tailrecursion/javastar, which
;; provided a map for this territory

(defn ^ConcurrentHashMap class-cache
  []
  (-> (.getDeclaredField clojure.lang.DynamicClassLoader "classCache")
      (doto (.setAccessible true))
      (.get nil)))

(defn source-object
  [class-name source]
  (proxy [SimpleJavaFileObject]
      [(java.net.URI/create (str "string:///"
                                 (.replace ^String class-name "." "/")
                                 (. JavaFileObject$Kind/SOURCE extension)))
       JavaFileObject$Kind/SOURCE]
    (getCharContent [_] source)))

(defn class-object
  "Returns a JavaFileObject to store a class file's bytecode."
  [class-name baos]
  (proxy [SimpleJavaFileObject]
      [(java.net.URI/create (str "string:///"
                                 (.replace ^String class-name "." "/")
                                 (. JavaFileObject$Kind/CLASS extension)))
       JavaFileObject$Kind/CLASS]
    (openOutputStream [] baos)))

(defn class-manager
  [cl manager cache]
  (proxy [ForwardingJavaFileManager] [manager]
    (getClassLoader [location]
      cl)
    (getJavaFileForOutput [location class-name kind sibling]
      (.remove (class-cache) class-name)
      (class-object class-name
                    (-> cache
                        (swap! assoc class-name (ByteArrayOutputStream.))
                        (get class-name))))))

(defn source->bytecode [name->source]
  (let [compiler (ToolProvider/getSystemJavaCompiler)
        diag     (DiagnosticCollector.)
        cache    (atom {})
        mgr      (class-manager nil (.getStandardFileManager compiler nil nil nil) cache)
        task     (.getTask compiler nil mgr diag nil nil
                           (->> name->source
                                (map #(source-object (key %) (val %)))
                                vec))]
    (if (.call task)
      (zipmap
       (keys @cache)
       (->> @cache
            vals
            (map #(.toByteArray ^ByteArrayOutputStream %))))
      (throw
       (RuntimeException.
        (apply str
               (interleave (.getDiagnostics diag) (repeat "\n\n"))))))))


(defn compile-bad
  []
  (try
    (source->bytecode {"foo" "xxxx"})
    (catch Throwable e
      (println 'XXX (.getMessage e)))))

(defn doit
  []
  (source->bytecode {"foo.Bar" "class Bar {}"})
  (require 'cider.nrepl.middleware.util.java)
  (compile-bad))

(defn doit2
  []
  (compile-bad)
  (require 'cider.nrepl.middleware.util.java)
  (compile-bad))
