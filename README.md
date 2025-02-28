# Asistente IA - WildCat Pro

Este proyecto esta relacionado a un 'Asistente IA', que permite hacer consultas sobre distintos documentos y links ingestados en una base de datos vectorial (RAG) 
para despues utilizar distintos modelos de la plataforma OpenAI para proporcionar una respuesta al usuario con sintaxis y semantica (prescindiendo de una persona
humana para obtener dicha informacion).


## Correr La Aplicacion:

Para correr la aplicacion, hacen falta tener fijadas las siguientes variables de entorno:
- OPEN_AI_API_KEY= Api keyque se obtiene desde el dashboard de la plataforma de Open AI.
- REDIS_CLIENT_HOST= Host del server de redis pues, para aplicar RAG, se utilizar la Redis Store de LangChain4j.
- REDIS_CLIENT_PASSWORD=
- REDIS_CLIENT_PORT=
- REDIS_CLIENT_USER=
- REDIS_INDEX= Index de Redis
- EMBEDDING_DIMENSIONS= Usualmente 384
- MONGO_SERVER_URL= URL del Server de mongo. Podria utilizarse otro tipo de base de datos (no necesariamente la de mongo atlas), ya que no se esta haciendo RAG con esa base de datops misma