# cider-vs-javac

This repository is used to demonstrate a bug occuring with
cider-nrepl-0.16.0-SNAPSHOT and virgil 0.1.7

virgil recompiles java source files on the fly. If a source file contains an
error, the javac compiler reports an IllegalAccessError instead of reporting the
error in the source file if cider-nrepl is also in use.



## How to reproduce

Run `lein repl` and then call `(doit)`:

```
recompiling all files in ["/home/ralf/cider-vs-javac/java-src"]
:reloading (cider-vs-javac.core)
nREPL server started on port 39323 on host 127.0.0.1 - nrepl://127.0.0.1:39323
ADD-CLASSPATH #object[java.net.URL 0x6b635b7b file:/usr/lib/jvm/oracle-java8-jdk-amd64/src.zip]
ADD-CLASSPATH #object[java.net.URL 0x3859f725 file:/usr/lib/jvm/oracle-java8-jdk-amd64/lib/tools.jar]
REPL-y 0.3.7, nREPL 0.2.12
Clojure 1.9.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_152-b16
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

cider-vs-javac.core=> (doit)
Compiling java-src
Compiling java-src-with-errors
An exception has occurred in the compiler (1.8.0_152). Please file a bug against the Java compiler via the Java bug reporting page (http://bugreport.java.com) after checking the Bug Database (http://bugs.java.com) for duplicates. Include your program and the following diagnostic in your report. Thank you.

IllegalAccessError tried to access class com.sun.tools.javac.util.Log$2 from class com.sun.tools.javac.util.Log  com.sun.tools.javac.util.Log.getWriter (Log.java:372)
cider-vs-javac.core=> 
recompiling all files in ["/home/ralf/cider-vs-javac/java-src"]
:reloading (cider-vs-javac.core)
nREPL server started on port 39323 on host 127.0.0.1 - nrepl://127.0.0.1:39323
ADD-CLASSPATH #object[java.net.URL 0x6b635b7b file:/usr/lib/jvm/oracle-java8-jdk-amd64/src.zip]
ADD-CLASSPATH #object[java.net.URL 0x3859f725 file:/usr/lib/jvm/oracle-java8-jdk-amd64/lib/tools.jar]
REPL-y 0.3.7, nREPL 0.2.12
Clojure 1.9.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_152-b16
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

cider-vs-javac.core=> (doit)
Compiling java-src
Compiling java-src-with-errors
An exception has occurred in the compiler (1.8.0_152). Please file a bug against the Java compiler via the Java bug reporting page (http://bugreport.java.com) after checking the Bug Database (http://bugs.java.com) for duplicates. Include your program and the following diagnostic in your report. Thank you.

IllegalAccessError tried to access class com.sun.tools.javac.util.Log$2 from class com.sun.tools.javac.util.Log  com.sun.tools.javac.util.Log.getWriter (Log.java:372)
cider-vs-javac.core=> 
```

## bisect

The problem does not occur with cider-nrepl 0.15.1.

With git bisect I've found out, that the issue has been introduced in [48dc5d24b0](https://github.com/clojure-emacs/cider-nrepl/commit/48dc5d24b0b20b637ba14b459549b53e5dbf5280)

If I comment out the code to cider.nrepl.middleware.uti.java/add-classpath!, the
problem disappears. In that case tools.jar and src.zip are not being added to
the classpath.

## Workaround 

Another workaround is to force loading of `cider.nrepl.middleware.util.java`  with :injections in `project.clj`:

```
   :injections [(println "INJECT: require cider.nrepl.middleware.util.java")
                (require 'cider.nrepl.middleware.util.java)]
```

`(doit)` now works:

```
(local) ~/cider-vs-javac (git)-[master] % lein repl
INJECT: require cider.nrepl.middleware.util.java
ADD-CLASSPATH #object[java.net.URL 0x5dd903be file:/usr/lib/jvm/oracle-java8-jdk-amd64/src.zip]
ADD-CLASSPATH #object[java.net.URL 0x5a6fa56e file:/usr/lib/jvm/oracle-java8-jdk-amd64/lib/tools.jar]

recompiling all files in ["/home/ralf/cider-vs-javac/java-src"]
:reloading (cider-vs-javac.core)
nREPL server started on port 39661 on host 127.0.0.1 - nrepl://127.0.0.1:39661
REPL-y 0.3.7, nREPL 0.2.12
Clojure 1.9.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_152-b16
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

cider-vs-javac.core=> (doit)
Compiling java-src
Compiling java-src-with-errors

RuntimeException /foo/Baz.java:5: error: cannot find symbol
        xxx = 5;
        ^
  symbol:   variable xxx
  location: class foo.Baz

  sun.reflect.NativeConstructorAccessorImpl.newInstance0 (NativeConstructorAccessorImpl.java:-2)
cider-vs-javac.core=> 
```

Please note the compiler giving the right error message ("cannot find symbol xxx").
