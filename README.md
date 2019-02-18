# TEXT SEARCH
comparing 3 methods of checking for similar text:
1. simple: we check for same words in our text database and the search text and return database text with more common words first
2. Levenshtein: use Levenshtein distance https://rosettacode.org/wiki/Levenshtein_distance#Scala to weight the database text against search text
3. mongo text: use mongo text search to get back database text similar to search text


#API
add data:
curl -d "the cow says moo" -H "Content-Type: text/plain" -X POST http://52.15.136.250:9021/index
curl -d "the cat and the hat" -H "Content-Type: text/plain" -X POST http://52.15.136.250:9021/index
curl -d " the dish ran away with the spoon" -H "Content-Type: text/plain"  -X POST http://52.15.136.250:9021/index

get results:

curl -d " a cat ran away" -H "Content-Type: text/plain" -X POST http://52.15.136.250:9021/simple_search
curl -d " a cat ran away" -H "Content-Type: text/plain" -X POST http://52.15.136.250:9021/levenshtein_search
curl -d " a cat ran away" -H "Content-Type: text/plain" -X POST http://52.15.136.250:9021/mongo_search

