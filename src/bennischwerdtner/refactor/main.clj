(ns bennischwerdtner.refactor.main
  (:require [bennischwerdtner.refactor.analyser :as ana]
            [clojure.string :as str])
  (:gen-class))

(defn -main
  [& args]
  (if-not (and (string? (first args))
               (not (clojure.string/blank? (first args))))
    (binding [*out* *err*]
      (println
       "help:

java -jar target/chunk-analyser-0.0.1-standalone.jar <request>

request: required string of your code request."))
    (ana/say-relevant-refactor-chunks {:request (first
                                                  args)})))
