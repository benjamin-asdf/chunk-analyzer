
# NOTICE

  The use and distribution terms for this software are covered by the
  Eclipse Public License 1.0 (https://opensource.org/license/epl-1-0/)
  which can be found in the file epl-v10.html at the root of this distribution.
  By using this software in any fashion, you are agreeing to be bound by
  the terms of this license.
  You must not remove this notice, or any other, from this software.

--------------------
 
# Concept

Say what chunks of a code base are required to update, given a refactor request in natural language. 
    
https://gist.github.com/VictorTaelin/23862a856250036f44fb8c5900fb796e


# Run

- pull this repository

## 1. Anthropic api key

- Either set an environment variable `ANTHROPIC_API_KEY`,
- or modify [this script](./secrets/api-key.sh) to output the api key

## 2. With Java


```
java -jar dist/chunk-analyser-0.0.1-standalone.jar "Fix todos"
```

## Or With Babashka

[See babashka](https://github.com/babashka/babashka)


```
bb -x bennischwerdtner.refactor.analyser/say-relevant-refactor-chunks --request "Fix todos"
```


# Build

- `bb build`, which see

# Time

Seems to be 5-10s.

# Cost

- It makes a single claude sonet api call per code refactor request

- input prompt is ca 7200 tokens.
- output is max 1024 but probably way less so let's say 400

- Claude 3.5 Sonnet 2024-10-22
- Million Input Tokens 3 USD
- Million Output tokens 15 USD

Comes to something in the range of 0.02-0.03 USD per code refactor request.
