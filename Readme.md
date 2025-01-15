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

