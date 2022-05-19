# GambleBot2
 discord bot for tf2 gamble events
 
 If for some reason you host your own bot using this, the database ER diagram is located in the base folder "current ER diagram" which I will try to remember to update for any commit that changes it. Don't forget your application.properties in the resources folder. 
 
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:your_connection_here
spring.datasource.username=your_username_here
spring.datasource.password=your_password_here
spring.main.web-application-type=NONE
spring.data.jpa.repositories.enabled=true
spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true

ER diagram is outdated in this commit
