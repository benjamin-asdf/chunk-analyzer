
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
See [bounty](https://gist.github.com/VictorTaelin/23862a856250036f44fb8c5900fb796e)


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

# Implementation - Concepts

- This uses **Anthropic public api**, asking a **model** to output *regex patterns*.

- The **regex patterns** are extracted and used to **filter** the input **source code chunks**. (by the program)

- The **initial prompt** is made from pre-generated / prompt engineered text that summarizes the task and code base.


# Build

- `bb build`, which see

- requires clojure tools to be installed.
  The aur package is `clojure`.
  ([Official install guide](https://clojure.org/guides/install_clojure))


# Time

Seems to be 5-10s.

# Cost

tldr: 0.02-0.03 USD per code refactor request

[Sheet](https://docs.google.com/spreadsheets/d/1iPO13RGSBKsKMUevKV7W2tytxssj5YvMlqcX31M8nFs/edit?usp=sharing)


- It makes a single claude sonet api call per code refactor request

- input prompt is ca 7200 tokens.
- output is max 1024 but probably way less so let's say 400

- Claude 3.5 Sonnet 2024-10-22; Million Input Tokens 3 USD; Million Output tokens 15 USD

Comes to something in the range of 0.02-0.03 USD per code refactor request.

# Limitations

- The inital prompt probably does not contain enough info for specific code base refactor tasks,
- You could try put in better info for example with something like treesitter data dump from https://github.com/bjsi/hvm3-refactor-bounty

