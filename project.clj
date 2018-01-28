(defproject minder "0.1.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-time "0.14.2"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [com.amazonaws/aws-lambda-java-core "1.1.0"]]
  :java-source-paths ["src/java"]
  :aot
  :all)
