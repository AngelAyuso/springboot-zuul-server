# springboot-zuul-server
Gateway o puerta de entrada a los microservicios desplegados en Eureka

Gateway o puerta de entrada a los microservicios desplegados en Eureka. Con este servicio enrutaremos dinámicamente la invocación a los servicios y tendremos balanceo de cargas.
Configurado como Servidor de Recursos con Spring Security Cloud oAuth2 para gestionar los accesos a los endpoints de los microservicios según el Token proporcionado por el Usuario. Además de gestionar si el Token es correcto.
