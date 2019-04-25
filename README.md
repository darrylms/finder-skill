# finderSkill
Amazon Alexa skill that calls the [finder-service](https://github.com/darrylms/finder-service).

It is built with [ASK SDK 2](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/welcome.html) and requires a copy of the [transport-api](https://github.com/darrylms/transport-api) in the local Maven repository (see the [pom](pom.xml) file).

# Alexa voice model

Invoke the the skill with
`ask route finder to get me a route`

Some other things to try:

`ask route finder to`...
- `get me a route from {fromLocation}`
- `plan a journey from {fromLocation}`
- `get me to {toLocation}`
- `get me from {fromLocation}`
- `get me somewhere`
- `plan a journey`
- `get me home`

If either the start point or the destination is missing, Alexa will prompt for each of these with questions like:

**Where are you travelling from?**

**What destination did you have in mind?**

   or
   
**What destination did you have in mind?**

Once all the slots are filled, Alexa should respond with appropriate directions.

If you do not like the route Alexa has chosen, you can respond with 

- `is there another way to get there`
- `i don't like that route get me another one`
- `i don't like that route get me another`
- `i don't like that route`

...and Alexa will try to look up an alternate route for you.


## Sample conversation


**User:** ask route finder to get me a route

**Alexa:** Where are you travelling from? 

**User:** 173 oxford street london

**Alexa:** What destination did you have in mind?

**User:** borough market

**Alexa:** Great, taking a look.

(processing response)
       
This journey takes 34 minutes. Walk for 10 minutes to Oxford Circus. Then, take the Central line to Bond Street. From there, take the Jubilee line to London Bridge. And then, walk for 9 minutes to 9 Stoney Street, Southwark.

**User:** I don't like that route

**Alexa:** No problem, I'll see if there's another way.
    
(processing response)
    
This journey takes 35 minutes. Walk for 10 minutes to Oxford Circus. From there, take the Bakerloo line to Waterloo. After that, take the Jubilee line to London Bridge. Next, walk for 9 minutes to 9 Stoney Street, Southwark.

**User:** I don't like that route

**Alexa:** Looking for an alternate route.

(processing response)

This journey takes 34 minutes. Walk for 17 minutes to Bond Street Station. Then after that, take the Jubilee line to London Bridge. And then, walk for 9 minutes to 9 Stoney Street, Southwark.

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for the full license text.

