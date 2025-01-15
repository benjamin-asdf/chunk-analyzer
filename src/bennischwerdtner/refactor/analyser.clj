(ns bennischwerdtner.refactor.analyser
  (:require
   [babashka.http-client :as http]
   [clojure.java.io :as io]
   [clojure.java.process :as p]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [cheshire.core :as json]))

(def api-key
  (memoize
   (fn []
     (or
      (System/getenv "ANTHROPIC_API_KEY")
      (clojure.string/trim (p/exec "secrets/api-key.sh"))))))

(defn chat-request!
  [opts]
  (http/request
    {:body (json/encode opts)
     :headers {"anthropic-version" "2023-06-01"
               "content-type" "application/json"
               "x-api-key" (api-key)}
     :method :post
     :uri "https://api.anthropic.com/v1/messages"}))

(defn read-resp [resp] (-> resp :body (json/decode keyword)))

(defn ask-for-regexes
  [code-refactor-request]
  (let [resp (chat-request!
               {:max_tokens 1024
                :messages
                  [{:content "hvm3 summary:" :role "user"}
                   {:content (slurp (io/resource
                                      "prompt/summary"))
                    :role "user"}
                   {:content "hvm3-truncated:" :role "user"}
                   {:content (slurp
                               (io/resource
                                 "prompt/hvm3-truncated"))
                    :role "user"}
                   {:content (slurp (io/resource
                                      "prompt/task"))
                    :role "user"}
                   {:content (str "Code refactor request: "
                                  code-refactor-request)
                    :role "user"}]
                :model "claude-3-5-sonnet-20241022"
                :stop_sequences ["END"]
                :temperature 0.1})]
    (-> (read-resp resp)
        :content
        peek
        :text)))

(defn extract-regexes
  [text]
  (keep (fn [txt]
          (try (re-pattern (edn/read-string txt))
               (catch Exception _)))
        (map second (re-seq #"regex: (.*)" text))))

(defn parse-chunks
  [chunk-text]
  (->>
    ;; first was not a chunk
    (drop 1
          (partition-by #(re-find #"^#\d+:$" %)
                        (clojure.string/split-lines
                          chunk-text)))
    (map (fn [lines]
           (if-let [chunk-id (when (empty? (rest lines))
                               (second
                                 (first (re-seq
                                          #"^#(\d+):$"
                                          (first lines)))))]
             [:chunk-id (parse-long chunk-id)]
             [:chunk-text (str/join "\n" lines)])))
    (reduce (fn [acc [type v]]
              (case type
                :chunk-id (assoc acc :current-chunk-id v)
                :chunk-text
                  (if-let [chunk-id (:current-chunk-id acc)]
                    (update acc
                            :chunks
                            conj
                            {:id chunk-id :text v})
                    acc)))
      {:chunks [] :current-chunk-id nil})
    :chunks))

(defn relevant-refactor-chunks
  [code-refactor-request]
  (let
    [chat (ask-for-regexes code-refactor-request)
     _ (do (println "Info")
           (println "Claude says: ")
           (println chat))
     patts (extract-regexes chat)
     source-code
       (slurp
         (io/resource
           "hvm3-chunked-codebase-snapshot-29-12-2024.txt"))
     chunks (parse-chunks source-code)]
    (into []
          (map :id
            (filter (comp (fn [source-code]
                            (some #(re-find % source-code)
                                  patts))
                          :text)
                    chunks)))))

(defn say-relevant-refactor-chunks
  [{:keys [request]}]
  (println "Asking for relevant refactor chunks for: "
           "'" (str/trim request)
           "'" "...")
  (let [relevant-chunks (relevant-refactor-chunks request)]
    (println (json/encode {:relevant-chunks
                             relevant-chunks}))))



(comment
  (chat-request! [{:content "I am a computer" :role "assistant"}])
  (chat-request!
   [{:content
     (slurp
      "/home/benj/repos/classify-blocks/resources/hvm3-chunked-codebase-snapshot-29-12-2024.txt")
     :role "user"}
    {:content
     "Summarize this code base suitable for consumption by refactor tools. Summarize type and function names consicely."
     :role "user"}])
  (def resp *1)
  (spit "resources/prompt/summary" (-> (read-resp resp) :content peek :text)))

(comment

  ;; --------
  ;; https://gist.github.com/VictorTaelin/23862a856250036f44fb8c5900fb796e#input
  ;;
  ;; I have way more chunks than he says in the example.
  ;; Not investigating. I leave it as is.
  ;; --------

  (ask-for-regexes
   "replace the 'λx body' syntax by '\\x body'")

  (def text "Let me help analyze this refactor request:\n\n1. Summary of refactor request:\nThe task requires changing lambda expression syntax from using 'λ' (lambda symbol) to using '\\' (backslash) as the lambda introducer, while keeping the parameter and body structure the same.\n\n2. Relationship to codebase:\nBased on the provided codebase summary and chunks, this involves:\n- Parser.hs (not shown in truncated snapshot but mentioned in summary) - where lambda expressions are parsed\n- Core type in Type.hs representing lambda terms (LAM constructor) \n- Show.hs (mentioned in summary) - for pretty printing lambdas\n- Related test files would also need updates\n\n3. Useful regex patterns to find relevant chunks:\n- Find λ character in string literals and comments: `λ`\n- Find lambda-related parser code: `parse.*[Ll]am`\n- Find lambda syntax in error messages or documentation: `\"λ[^\"]*\"`\n- Find lambda-related constructor patterns: `LAM|[Ll]am`\n- Find pretty printing of lambdas: `show.*[Ll]am`\n\n4. Solution regex patterns:\n\nregex: \"λ\"\nregex: \"parse[A-Za-z]*[Ll]am[A-Za-z]*\"\nregex: \"\\\"λ[^\\\"]*\\\"\"\nregex: \"(LAM|[Ll]am)\"\nregex: \"show[A-Za-z]*[Ll]am[A-Za-z]*\"\n\nThese patterns will help identify code chunks that need to be updated to change the lambda syntax. The key is to find both the parsing and pretty printing logic, as well as any documentation or error messages that might reference the lambda symbol.")

  (def patts (extract-regexes text))

  (def source-code
    (slurp
     (io/resource
      "hvm3-chunked-codebase-snapshot-29-12-2024.txt")))

  (def chunks (parse-chunks source-code))

  (into []
        (map :id
             (filter (comp (fn [source-code]
                             (some #(re-find % source-code)
                                   patts))
                           :text)
                     chunks)))

  [15 34 83 88 112 117 123 134 154 184 190 203 215 220 254 255 256 263 284 285 286 287 288 289 295 300 310 344 353 358 364 369 374 394 395 396 397 398 404 414 433 455 480 487 491 493 505])
