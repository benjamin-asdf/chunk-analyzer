
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

  (let [opts
        {:messages
         [{:content "hvm3 summary:" :role "user"}
          {:content (slurp (io/resource "prompt/summary"))
           :role "user"}
          {:content "hvm3-truncated:" :role "user"}
          {:content (slurp (io/resource
                            "prompt/hvm3-truncated"))
           :role "user"}
          {:content (slurp (io/resource "prompt/task"))
           :role "user"}
          {:content (str "Code refactor request: "
                         "do the foo with the bar loll")
           :role "user"}]
         :model "claude-3-5-sonnet-20241022"}]
    ;; curl
    ;; https://api.anthropic.com/v1/messages/count_tokens
    ;; \ --header "x-api-key: $ANTHROPIC_API_KEY" \
    ;; --header "content-type: application/json" \
    ;; --header "anthropic-version: 2023-06-01" \ --data
    ;; '{
    ;;          "model": "claude-3-5-sonnet-20241022",
    ;;          "system": "You are a scientist",
    ;;          "messages": [{
    ;;                        "role": "user", "content":
    ;;                        "Hello, Claude"
    ;;                        }]
    ;;          }'
    (http/request
     {:body (json/encode opts)
      :headers {"anthropic-version" "2023-06-01"
                "content-type" "application/json"
                "x-api-key" (api-key)}
      :method :post
      :uri
      "https://api.anthropic.com/v1/messages/count_tokens"}))

  *1
  (def resp *1)
  (-> resp :body (json/decode keyword))
  {:input_tokens 7187}




  )
