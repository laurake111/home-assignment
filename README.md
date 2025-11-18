# Software Engineer Test Assignment - Laura Zirk

### Application description:

* Account service keeps track of accounts, their balances, and transactions.
* Account service must publish all insert and update operations to RabbitMQ.


### How to run application in Docker

In your terminal (in the root of the project) run:
1. `docker compose build`
2. `docker compose up`

### How to run tests in Docker

Please run the integration tests separately,   
because when tests are ran together the RabbitMQ integration test fails, because there already is another consumer on it
1. `docker compose run --rm app test --tests "ee.tuum.assignment.integration.AccountIntegrationTest`
2. `docker compose run --rm app test --tests "ee.tuum.assignment.integration.TransactionIntegrationTest`
3. `docker compose run --rm app test --tests "ee.tuum.assignment.integration.RabbitMQTest`

## Additional Tasks

### Explanation of important choices in this solution
1. **Using `SELECT ... FOR UPDATE` when creating a new transaction**  
   This allows me to lock the balance row in the database when a new transaction is made,  preventing other transactions from modifying the row until the current transaction is committed or rolled back.
2. **Using `@Transactional` on service methods, so when something in the function fails. Everything will be rolled back.**    
   When anything in a method fails (throws an Exception) the changes already made in the DB will be rolled back.
3. **Using a basic custom exception handler to return simple readable API errors**  
   The exception handler converts validation (Jakarta validation), enum errors (Direction, Currency) and IllegalArgument exception errors into a simple 400 responses with custom messages.

---  

### Estimate how many transactions your account application can handle per second on your development machine

On average, it takes ~15ms for one transaction. So I estimate that the application can make around 66 transactions per second (1/0.015) on my development machine
  
---  

### Describe what you have to consider to be able to scale applications horizontally
1. Need to add a load balancer in front of the application. Splits requests between machines.
2. Currently, all requests are handled by a single database. To scale it, I need to consider database sharding (split large database into smaller components). Transaction handling will be more complex
3. Consider using Kubernetes to make scaling easier
4. My application is currently stateless (everything is stored in the database), making scaling this application easier.

---  

### Explanation of the usage of AI
I use AI (ChatGPT) as a helper tool. AI to me is like a rubber duck, which I can share ideas with and helps me understand concepts faster and more clearly.  
For example, without AI it would have taken me longer to, use/understand myBatis as I have never used it before.  
But because AI has no context to the actual code I am writing, everything AI generated has to be taken with a grain of salt.   
Most of the time I needed to teach ChatGPT what are the right solutions to my problems.  
In summary, AI supported my learning and thinking process. Implementation was fully done by me.
