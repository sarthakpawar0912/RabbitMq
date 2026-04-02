# 🐰 RabbitMQ Microservices Project

> **A beginner-friendly microservices project demonstrating asynchronous communication between an Order Service and a Payment Service using RabbitMQ as the message broker.**

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3--management-ff6600?style=for-the-badge&logo=rabbitmq)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker)](https://www.docker.com/)

---

## 📖 Table of Contents

- [What is RabbitMQ?](#-what-is-rabbitmq)
- [Why Use RabbitMQ?](#-why-use-rabbitmq)
- [Core Concepts](#-core-concepts)
- [Types of Exchanges](#-types-of-exchanges)
- [Message Flow](#-message-flow-how-a-message-travels)
- [Benefits of RabbitMQ](#-benefits-of-rabbitmq)
- [About This Project](#-about-this-project)
- [Project Architecture](#-project-architecture)
- [Project Flow](#-project-flow-step-by-step)
- [Tech Stack](#️-tech-stack)
- [How to Run](#-how-to-run-the-project)
- [API Endpoints](#-api-endpoints)
- [Screenshots](#-screenshots)
- [Additional Notes](#-additional-notes)

---

<br>

# 🧠 SECTION 1 — Understanding RabbitMQ

---

## 🐇 What is RabbitMQ?

**RabbitMQ** is an open-source **message broker** — a middleman that helps different applications (or microservices) talk to each other by sending and receiving **messages** through **queues**.

Think of it like a **post office** for your applications:

> 📬 **Analogy:** Imagine you write a letter (message) and drop it at the post office (RabbitMQ). The post office doesn't deliver it immediately — it stores the letter safely and delivers it to the right person (consumer) when they're ready to receive it. The sender doesn't need to wait for the receiver to be available.

In software terms:
- **Without RabbitMQ:** Service A calls Service B directly. If B is down or slow, A is stuck waiting.
- **With RabbitMQ:** Service A drops a message in the queue and moves on. Service B picks it up whenever it's ready.

```
❌ WITHOUT RabbitMQ (Tight Coupling):
┌──────────┐    direct call    ┌──────────┐
│ Service A │ ───────────────► │ Service B │   ⚠️ If B is down, A fails too!
└──────────┘                   └──────────┘

✅ WITH RabbitMQ (Loose Coupling):
┌──────────┐    message    ┌──────────┐    message    ┌──────────┐
│ Service A │ ────────────►│ RabbitMQ │ ────────────► │ Service B │
└──────────┘               └──────────┘               └──────────┘
   (Producer)              (Message Broker)              (Consumer)
                          📦 Stores messages             Picks up when ready
                             safely in queue
```

---

## 🤔 Why Use RabbitMQ?

### The Problem It Solves

Imagine you're building an **e-commerce application**. When a customer places an order, you need to:

1. ✅ Save the order in database
2. 💳 Process the payment
3. 📧 Send confirmation email
4. 📦 Notify the warehouse
5. 📊 Update analytics

**Without a message queue**, the order API would need to do ALL of this synchronously — the customer waits while every step completes. If the email server is slow, the customer's checkout is slow too!

**With RabbitMQ**, the order API just saves the order and drops messages into queues. Each service picks up its messages independently. The customer gets an instant response! ⚡

> 📱 **WhatsApp Analogy:** When you send a WhatsApp message, you don't wait for the other person to read it. WhatsApp delivers it, stores it, and the other person reads it when they're online. RabbitMQ works exactly like this for your backend services!

---

## 📦 What is a Message Queue?

A **message queue** is like a waiting line (queue) where messages stand in order:

```
Message Queue = A line of messages waiting to be processed

    ┌─────────────────────────────────────────────┐
    │  MSG 1  │  MSG 2  │  MSG 3  │  MSG 4  │ ... │
    └─────────────────────────────────────────────┘
       ▲                                        │
       │                                        ▼
    Messages                               Messages
    come IN                                go OUT
    (from Producer)                        (to Consumer)

    📝 FIFO = First In, First Out (like a real queue!)
```

**Key properties:**
- Messages are stored **safely** until consumed
- Messages are processed in **order** (FIFO — First In, First Out)
- If the consumer crashes, messages **wait** in the queue (no data loss!)
- Multiple consumers can read from the **same queue** (load balancing)

---

## 🔧 Core Concepts

### 1. 📤 Producer

The **Producer** is the application that **sends** (publishes) messages.

> 🏪 **Analogy:** You are the producer when you drop a parcel at FedEx. You hand over the package and walk away — you don't deliver it yourself.

```
┌──────────────┐          message
│   Producer   │  ──────────────────►  📬 RabbitMQ
│ (Order Svc)  │   "New order #101"
└──────────────┘
```

**In this project:** `OrderServiceRabbit` is the **Producer** — it sends order details to RabbitMQ.

---

### 2. 📥 Consumer

The **Consumer** is the application that **receives** and **processes** messages from the queue.

> 📦 **Analogy:** The person who receives the FedEx parcel at their doorstep is the consumer. They open it and take action (use the product).

```
                              message
📬 RabbitMQ  ──────────────────────►  ┌──────────────┐
              "New order #101"        │   Consumer   │
                                      │ (Payment Svc)│
                                      └──────────────┘
```

**In this project:** `PaymentServiceRabbit` is the **Consumer** — it listens for order messages and processes payments.

---

### 3. 📋 Queue

A **Queue** is a **buffer** that stores messages. It's the actual "line" where messages wait.

> 📬 **Analogy:** The mailbox outside your house. Letters (messages) pile up in it until you pick them up.

```
Queue: "order-queue"
┌─────────────────────────────────────────────────┐
│ Order#101 │ Order#102 │ Order#103 │ Order#104   │
└─────────────────────────────────────────────────┘
     ▲                                       │
     │ Messages arrive                       │ Messages delivered
     │ from Exchange                         ▼ to Consumer
```

**In this project:** The queue is named `order-queue`.

---

### 4. 🔀 Exchange

An **Exchange** is the **router** — it receives messages from the producer and decides **which queue(s)** to send them to based on rules.

> 🏤 **Analogy:** The sorting center at the post office. When a letter arrives, the sorting center reads the address (routing key) and puts it in the right delivery bag (queue).

```
                    ┌─────────────────────┐
                    │      Exchange       │
  Message ────────► │   (Router/Sorter)   │ ────────► Queue A
                    │                     │ ────────► Queue B
                    │  "Where should this │ ────────► Queue C
                    │   message go?"      │
                    └─────────────────────┘
```

**In this project:** The exchange is named `order-exchange` and is of type **TopicExchange**.

---

### 5. 🔑 Routing Key

A **Routing Key** is a label/tag attached to each message that the exchange uses to decide which queue gets the message.

> 🏷️ **Analogy:** The ZIP code on your letter. The post office reads the ZIP code to decide which delivery route to use.

**In this project:** The routing key is `order-routing`.

---

### 6. 🔗 Binding

A **Binding** is the link/rule that connects an exchange to a queue. It tells the exchange: _"If a message has THIS routing key, send it to THAT queue."_

> 🔌 **Analogy:** The wire connecting a switch to a light bulb. The binding connects the exchange (switch) to the queue (bulb).

```
  Exchange ──── Binding (routing key = "order-routing") ────► Queue
  "order-exchange"                                          "order-queue"
```

**In this project:** The binding connects `order-exchange` to `order-queue` using routing key `order-routing`.

---

## 🔀 Types of Exchanges

This is one of the **most important concepts** in RabbitMQ. The exchange type determines **how messages are routed** to queues.

---

### 1. 🎯 Direct Exchange

**Definition:** Routes messages to queues where the **routing key exactly matches** the binding key.

**When to use:** When you want a message to go to **one specific queue**.

> 📬 **Analogy:** Sending a letter with a specific apartment number. Only that exact apartment gets the letter.

**Example:** Send payment messages to the payment queue, and email messages to the email queue.

```
                         ┌──────────────────┐
                         │  Direct Exchange  │
                         └────────┬─────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
     routing_key =        routing_key =        routing_key =
      "payment"            "email"              "sms"
              │                   │                   │
              ▼                   ▼                   ▼
     ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
     │ Payment Queue│   │ Email Queue  │   │  SMS Queue   │
     └──────────────┘   └──────────────┘   └──────────────┘
```

---

### 2. 📢 Fanout Exchange

**Definition:** Routes messages to **ALL queues** bound to it. Ignores routing keys completely.

**When to use:** When you want **every** consumer/queue to get a copy of the message (broadcasting).

> 📺 **Analogy:** A TV broadcast — when a channel broadcasts a show, every TV tuned to that channel receives it.

**Example:** When a new user registers, send notification to email service, analytics service, and welcome service — all at once.

```
                         ┌──────────────────┐
  "New User!" ──────────►│ Fanout Exchange  │
                         └────────┬─────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │             │             │
                    ▼             ▼             ▼
           ┌─────────────┐ ┌──────────┐ ┌───────────────┐
           │ Email Queue │ │Analytics │ │ Welcome Queue │
           │             │ │  Queue   │ │               │
           └─────────────┘ └──────────┘ └───────────────┘

           📌 ALL queues get the SAME message!
```

---

### 3. 📝 Topic Exchange

**Definition:** Routes messages based on **wildcard pattern matching** of the routing key.

**Wildcards:**
- `*` (star) = matches **exactly one** word
- `#` (hash) = matches **zero or more** words

**When to use:** When you want **flexible routing** — some queues get some messages based on patterns.

> 📰 **Analogy:** A newspaper subscription. You subscribe to "sports.*" and get all sports news (sports.cricket, sports.football), but not politics news.

**Example:** Route logs based on severity and source.

```
                         ┌──────────────────┐
                         │  Topic Exchange  │
                         └────────┬─────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
     Binding Key:          Binding Key:        Binding Key:
     "order.*"             "order.payment"     "#.error"
              │                   │                   │
              ▼                   ▼                   ▼
     ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
     │  All Orders  │   │ Payment Only │   │  All Errors  │
     │    Queue     │   │    Queue     │   │    Queue     │
     └──────────────┘   └──────────────┘   └──────────────┘

  Message with routing key "order.payment":
    ✅ Goes to "All Orders Queue"    (matches "order.*")
    ✅ Goes to "Payment Only Queue"  (matches "order.payment")
    ❌ Does NOT go to "All Errors"   (no ".error" pattern)
```

**🔥 This project uses Topic Exchange!** (Exchange: `order-exchange`, Routing Key: `order-routing`)

---

### 4. 📋 Headers Exchange

**Definition:** Routes messages based on **message header attributes** instead of routing keys.

**When to use:** When routing logic is too complex for a simple routing key — you need **multiple attributes** to decide.

> 🏷️ **Analogy:** A sorting machine at Amazon warehouse that checks multiple labels (fragile? heavy? international?) to decide which conveyor belt to use.

**Example:** Route based on headers like `format=pdf` AND `type=report`.

```
                         ┌──────────────────┐
                         │ Headers Exchange │
                         └────────┬─────────┘
                                  │
              Headers:            │          Headers:
           format=pdf ────────────┼────── format=json
           type=report            │       type=log
              │                              │
              ▼                              ▼
     ┌──────────────┐              ┌──────────────┐
     │  PDF Reports │              │  JSON Logs   │
     │    Queue     │              │    Queue     │
     └──────────────┘              └──────────────┘

     📌 Routing key is IGNORED — only headers matter!
```

---

### 📊 Exchange Types — Quick Comparison

| Feature | Direct | Fanout | Topic | Headers |
|---------|--------|--------|-------|---------|
| **Routing Based On** | Exact routing key | Nothing (broadcast) | Pattern matching | Header attributes |
| **Use Case** | Point-to-point | Broadcast to all | Flexible routing | Complex routing |
| **Routing Key Used?** | ✅ Yes (exact match) | ❌ No (ignored) | ✅ Yes (wildcards) | ❌ No (headers) |
| **Speed** | ⚡ Fastest | ⚡ Fast | 🔄 Moderate | 🐢 Slowest |
| **Complexity** | Simple | Simplest | Moderate | Complex |

---

## 🔄 Message Flow — How a Message Travels

This is the **complete lifecycle** of a message in RabbitMQ:

```
┌──────────┐     ┌──────────────┐     ┌───────────┐     ┌──────────┐
│          │     │              │     │           │     │          │
│ PRODUCER │────►│   EXCHANGE   │────►│   QUEUE   │────►│ CONSUMER │
│          │     │              │     │           │     │          │
└──────────┘     └──────────────┘     └───────────┘     └──────────┘
                        │                   │
                   Routing Key         FIFO Order
                   decides WHERE       Messages wait
                   message goes        until consumed
```

### Detailed Flow:

```
Step 1️⃣                Step 2️⃣              Step 3️⃣             Step 4️⃣

 Producer              Exchange            Queue               Consumer
 publishes             routes the          stores the          picks up
 message               message             message             message

 ┌─────────┐          ┌─────────┐         ┌─────────┐        ┌─────────┐
 │  "Hey,  │  ─────►  │ Check   │ ─────►  │ Wait in │ ─────► │ Process │
 │  new    │  routing  │ routing │ binding │  line   │  pull  │   and   │
 │  order" │   key     │  key    │  rules  │  (FIFO) │  msg   │  done!  │
 └─────────┘          └─────────┘         └─────────┘        └─────────┘
```

---

## ✅ Benefits of RabbitMQ

### 1. ⚡ Asynchronous Communication
Services don't wait for each other. The producer sends a message and immediately moves on.
```
Synchronous:  A ──wait──► B ──wait──► C   (Slow! ⏱️)
Asynchronous: A ──msg──► Queue        (Instant! ⚡)
                         B picks up later
                         C picks up later
```

### 2. 🔓 Decoupling Services
Services don't need to know about each other. They only know about the queue.
```
❌ Tight Coupling:    OrderService.callPaymentService()
✅ Loose Coupling:    OrderService → Queue → PaymentService
```

### 3. 📈 Scalability
Need to handle more messages? Just add more consumers! RabbitMQ will distribute messages among them.
```
                          ┌──────────────┐
                     ┌───►│ Consumer 1   │
                     │    └──────────────┘
┌─────────┐         │    ┌──────────────┐
│  Queue   │─────────┼───►│ Consumer 2   │   Load Balanced!
└─────────┘         │    └──────────────┘
                     │    ┌──────────────┐
                     └───►│ Consumer 3   │
                          └──────────────┘
```

### 4. 🛡️ Reliability
Messages are **persisted** in the queue. Even if a consumer crashes, messages are safe and will be redelivered.

---

<br>

# 🧩 SECTION 2 — About This Project

---

## 📌 Overview

This project demonstrates a **real-world microservices architecture** where two independent Spring Boot services communicate asynchronously through RabbitMQ:

- **Order Service** receives orders from users via REST API and publishes them as messages
- **Payment Service** listens for order messages and processes payments automatically

> 💡 The services are completely **decoupled** — they don't call each other directly. They only communicate through RabbitMQ!

---

## 📂 Project Structure

```
rabiitmq/
│
├── 📁 OrderServiceRabbit/           ← Producer (Port 8080)
│   ├── 📁 config/
│   │   └── RabbitConfig.java        ← RabbitMQ configuration (Exchange, Queue, Binding)
│   ├── 📁 controller/
│   │   └── OrderController.java     ← REST API endpoint (POST /orders)
│   ├── 📁 model/
│   │   └── Order.java               ← Order data model (orderId, product, amount)
│   ├── 📁 service/
│   │   └── OrderProducer.java       ← Sends messages to RabbitMQ
│   └── application.properties       ← App config (RabbitMQ connection)
│
├── 📁 PaymentServiceRabbit/         ← Consumer (Port 8081)
│   ├── 📁 config/
│   │   └── RabbitConfig.java        ← RabbitMQ configuration (same exchange/queue)
│   ├── 📁 model/
│   │   └── Order.java               ← Order data model (same as OrderService)
│   ├── 📁 service/
│   │   └── PaymentConsumer.java     ← Listens to queue and processes payments
│   └── application.properties       ← App config (port 8081, RabbitMQ connection)
│
└── 📄 README.md
```

---

## 🧩 Services

### 1. 📤 Order Service (Producer) — Port `8080`

| Component | File | Responsibility |
|-----------|------|---------------|
| **Controller** | `OrderController.java` | Exposes `POST /orders` endpoint, accepts order JSON |
| **Service** | `OrderProducer.java` | Sends order message to RabbitMQ via `RabbitTemplate` |
| **Config** | `RabbitConfig.java` | Defines exchange, queue, binding, and JSON converter |
| **Model** | `Order.java` | Data class with `orderId`, `product`, `amount` |

**How it sends messages:**
1. User hits `POST /orders` with order JSON body
2. Controller receives the request and calls `OrderProducer.placeOrder(order)`
3. `OrderProducer` uses `RabbitTemplate.convertAndSend()` to publish the order:
   - **Exchange:** `order-exchange`
   - **Routing Key:** `order-routing`
   - **Payload:** Order object serialized to JSON using Jackson

---

### 2. 📥 Payment Service (Consumer) — Port `8081`

| Component | File | Responsibility |
|-----------|------|---------------|
| **Service** | `PaymentConsumer.java` | Listens to `order-queue` and processes payments |
| **Config** | `RabbitConfig.java` | Same exchange/queue configuration as Order Service |
| **Model** | `Order.java` | Same Order data class for deserialization |

**How it consumes messages:**
1. `PaymentConsumer` class is annotated with `@RabbitListener(queues = "order-queue")`
2. When a message arrives in `order-queue`, RabbitMQ delivers it to this listener
3. Jackson automatically converts the JSON message back to an `Order` object
4. `processPayment()` method executes:
   - If `amount > 0` → **Payment Success** ✅
   - If `amount <= 0` → **Payment Failed** ❌

---

## 🔄 Project Flow (Step-by-Step)

```
┌─────────┐        ┌──────────────────┐        ┌────────────────┐        ┌─────────────────┐
│  👤 User │        │ 📤 Order Service │        │  🐰 RabbitMQ   │        │ 💳 Payment Svc  │
│ (Client) │        │   (Port 8080)    │        │  (Port 5672)   │        │   (Port 8081)   │
└────┬─────┘        └────────┬─────────┘        └───────┬────────┘        └────────┬────────┘
     │                       │                          │                          │
     │  1️⃣ POST /orders      │                          │                          │
     │  {orderId, product,   │                          │                          │
     │   amount}             │                          │                          │
     │ ─────────────────────►│                          │                          │
     │                       │                          │                          │
     │                       │  2️⃣ convertAndSend()     │                          │
     │                       │  exchange: order-exchange │                          │
     │                       │  key: order-routing       │                          │
     │                       │ ────────────────────────►│                          │
     │                       │                          │                          │
     │  3️⃣ "Order created    │                          │  4️⃣ Route message        │
     │       successfully"   │                          │  exchange → queue         │
     │ ◄─────────────────────│                          │  "order-queue"            │
     │                       │                          │ ────────────────────────► │
     │                       │                          │                          │
     │                       │                          │                     5️⃣ processPayment()
     │                       │                          │                     Check amount > 0?
     │                       │                          │                     ✅ Success / ❌ Fail
     │                       │                          │                          │
```

### Step-by-Step Walkthrough:

| Step | What Happens | Where |
|------|-------------|-------|
| **1** | User sends a `POST` request to `/orders` with order details (JSON) | Client → Order Service |
| **2** | `OrderController` receives the request and calls `OrderProducer.placeOrder()` | Order Service |
| **3** | `OrderProducer` uses `RabbitTemplate` to send the order as a JSON message to `order-exchange` with routing key `order-routing` | Order Service → RabbitMQ |
| **4** | `order-exchange` (TopicExchange) matches the routing key and delivers the message to `order-queue` | RabbitMQ |
| **5** | `PaymentConsumer` (listening on `order-queue` via `@RabbitListener`) receives the message | RabbitMQ → Payment Service |
| **6** | `processPayment()` checks if `amount > 0` and prints success or failure | Payment Service |

---

## ⚙️ Tech Stack

| Technology | Purpose |
|-----------|---------|
| **Java 21** | Programming language |
| **Spring Boot 4.0.5** | Application framework |
| **Spring AMQP** | RabbitMQ integration (`spring-boot-starter-amqp`) |
| **RabbitMQ 3-management** | Message broker with management UI |
| **Docker** | Running RabbitMQ container |
| **Jackson** | JSON serialization/deserialization of messages |
| **Maven** | Build tool and dependency management |
| **REST API** | HTTP endpoint for placing orders |

---

## 🔑 RabbitMQ Configuration

| Property | Value |
|----------|-------|
| **Exchange Name** | `order-exchange` |
| **Exchange Type** | `TopicExchange` |
| **Queue Name** | `order-queue` |
| **Routing Key** | `order-routing` |
| **Message Format** | JSON (via `JacksonJsonMessageConverter`) |
| **Host** | `localhost` |
| **Port** | `5672` (AMQP) / `15672` (Management UI) |
| **Username** | `guest` |
| **Password** | `guest` |

---

<br>

# 🚀 How to Run the Project

---

## 📋 Prerequisites

- ☕ **Java 21** installed
- 🐳 **Docker** installed and running
- 📦 **Maven** installed (or use the Maven wrapper)

---

## Step 1: 🐰 Run RabbitMQ Using Docker

Open your terminal and run:

```bash
docker run -d --hostname rabbitmq --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

| Port | Purpose |
|------|---------|
| `5672` | AMQP protocol (application communication) |
| `15672` | Management UI (web dashboard) |

✅ **Verify:** Open [http://localhost:15672](http://localhost:15672) — you should see the RabbitMQ login page.

---

## Step 2: 🟢 Run the Services

### Start Order Service (Producer)

```bash
cd OrderServiceRabbit
./mvnw spring-boot:run
```

> Runs on **port 8080**

### Start Payment Service (Consumer)

```bash
cd PaymentServiceRabbit
./mvnw spring-boot:run
```

> Runs on **port 8081**

---

## Step 3: 🌐 Access RabbitMQ Dashboard

| Field | Value |
|-------|-------|
| **URL** | [http://localhost:15672](http://localhost:15672) |
| **Username** | `guest` |
| **Password** | `guest` |

From the dashboard you can monitor:
- 📊 **Queues** — see messages waiting and being consumed
- 🔀 **Exchanges** — see `order-exchange` listed
- 📈 **Message rates** — real-time graphs of message flow

---

## Step 4: 🧪 Test the API

Send a POST request to the Order Service:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 101,
    "product": "Laptop",
    "amount": 75000.00
  }'
```

**Expected Response:**
```
Order created successfully
```

**Console Output — Order Service:**
```
Order Sent 101
```

**Console Output — Payment Service:**
```
processing -- 101
Payment success for order 101
```

---

### Test with Invalid Amount (amount ≤ 0):

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 102,
    "product": "Free Sample",
    "amount": 0
  }'
```

**Console Output — Payment Service:**
```
processing -- 102
Payment failed...
```

---

## 📡 API Endpoints

### Order Service (Port 8080)

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|-------------|
| `POST` | `/orders` | Place a new order | JSON (see below) |

### Request Body Schema:

```json
{
  "orderId": 101,
  "product": "Laptop",
  "amount": 75000.00
}
```

| Field | Type | Description |
|-------|------|-------------|
| `orderId` | `int` | Unique identifier for the order |
| `product` | `String` | Name of the product |
| `amount` | `double` | Order amount (must be > 0 for successful payment) |

### Response:

| Status | Body |
|--------|------|
| `200 OK` | `Order created successfully` |

---

## 📸 Screenshots

### RabbitMQ Management Dashboard
> 📌 _Add your screenshot here after running the project_
>
> ![RabbitMQ Dashboard](screenshots/rabbitmq-dashboard.png)

### API Testing (Postman / cURL)
> 📌 _Add your screenshot here showing the API request and response_
>
> ![API Testing](screenshots/api-testing.png)

### Console Logs
> 📌 _Add your screenshot showing the producer and consumer console output_
>
> ![Console Output](screenshots/console-output.png)

---

## 🧠 Additional Notes

### 💡 How Messages Are Processed

1. **Serialization:** When the producer sends an `Order` object, `JacksonJsonMessageConverter` converts it to a **JSON string** before sending it to RabbitMQ.

2. **Deserialization:** When the consumer receives the message, the same converter parses the JSON back into an `Order` object.

3. **Acknowledgment:** By default, Spring AMQP uses **auto-acknowledgment** — once the consumer method completes without throwing an exception, the message is acknowledged and removed from the queue.

### ⚡ Asynchronous Communication

- The `POST /orders` API returns **immediately** after publishing the message — it does **not** wait for the payment to be processed.
- The Payment Service processes messages **independently** and at its own pace.
- If the Payment Service is down, messages **queue up safely** in RabbitMQ and are delivered once it comes back online.

### 🔒 Why Topic Exchange in This Project?

Although this project uses a simple single routing key (`order-routing`), the **TopicExchange** allows for future extensibility:
- Add `order.payment` routing key for payment-specific messages
- Add `order.notification` for notification messages
- Consumers can use wildcards like `order.*` to receive all order-related messages

---

## 🤝 Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

---

## 📜 License

This project is open-source and available under the [MIT License](LICENSE).

---

<div align="center">

**⭐ If you found this project helpful, give it a star on [GitHub](https://github.com/sarthakpawar0912/RabbitMq)! ⭐**

Made with ❤️ by [Sarthak Pawar](https://github.com/sarthakpawar0912)

</div>
