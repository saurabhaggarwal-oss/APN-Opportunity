### **Master Prompt - APN ACE Net New Workload Opportunities**

**Task:** Categorize AWS resource cost data into net new workload opportunities for APN ACE submissions.

---

#### **1. Row Integrity & Grouping Rules (Mandatory):**
* **Zero Data Loss:** Return **every single row** provided in the input. Do not truncate the table.
* **Row-Level Grouping Consistency:** Identify resources belonging to the same system based on `FINAL_NAME_TAG`, `Account ID`, and `Region`. **All rows within the same logical group must share the exact same "Workload Title" and "Workload Description".**
* **Column Preservation:** Append "Workload Title" and "Workload Description" as the final two columns. Do not modify original columns.

---

#### **2. Entity Intelligence & Cleaning:**
* **System Name Extraction:** Analyze `FINAL_NAME_TAG`. Strip technical suffixes and numerical IDs (e.g., `prod01-mdb7-xvdb` becomes **Movista Production**).
* **Service Identification:** Use official AWS Service names (e.g., Amazon RDS, Amazon EC2, Amazon ElastiCache).
* **Contextual Enrichment:** Leverage `OS`, `Region`, and `Instance Type` when available to add technical precision.

---


#### **3. Net New Workload Principles:**
* **Project-Oriented Language:** These are NEW workloads being built on AWS, not existing operations
* **Building/Creating Focus:** Emphasize what's being architected and constructed
* **Business Capability Enablement:** Highlight the new business outcomes these workloads enable
* **Greenfield Framing:** Position as net new AWS consumption and capability

---

#### **4. Formatting & Tone (Strict Technical Standards):**
* **Third-Person Customer Perspective:** Strictly avoid "we," "our," or "I." The actor is "The customer" or "The project."
* **No Marketing Language:** Delete adjectives like "mission-critical," "seamless," "enhanced," "optimal," "robust," or "cutting-edge."
* **Selective Architecture Terms:** Use **Highly Available** or **Fault-Tolerant** *only* if the workload involves clusters, multi-node setups, or cross-AZ redundancy.
* **Engineering Terminology:** Use precise terms such as **Source Network Address Translation (SNAT), I/O Throughput, Read-Replica Offloading, Schema Parity, Persistent Block Storage, DAG Scheduling,** and **Event Stream Replication.**

---

#### **5. Column Content Structure:**

**Column: Workload Title (Net New Project Format):**

* **Formula (Two Acceptable Patterns):**
  1. **Project Verb Pattern:** `[Build/Architect/Launch] [Technical Component/Architecture] for [Clean System Name] + [Business Function]`
  2. **Noun-Based Pattern:** `[Technical Component/Architecture] for [Clean System Name] + [Business Function]`

* **Net New Compatible Verbs:**
  * **Approved Verbs:** Build, Architect, Launch, Establish, Create, Develop
  * **Context:** These verbs indicate NEW workload creation, not ongoing operations
  * **Alternative:** Use noun-based titles without verbs

* **Technical Clarity Requirements:**
  * **Lead with technical differentiation:** MongoDB-Compatible Cluster, Workflow Orchestration Backend, ML Model Training Environment
  * **Incorporate distinguishing details:** Database Engine, Instance Family, OS, Architecture
  * **Omit redundant information:** Do not include "using [Service]" or "in [Region]" in titles
  * **Keep titles under 12 words**
  
* **Examples:**
  * ✅ **Good:** "Build MongoDB-Compatible Multi-Instance Cluster for Beem Project"
  * ✅ **Good:** "Architect ML Model Training Environment for Branch Analytics"
  * ✅ **Good:** "Launch Workflow Orchestration Backend for Branch Airflow Staging"
  * ✅ **Good:** "PostgreSQL Burstable Instance for CEAT Halol Development"
  * ✅ **Good:** "SFTP File Transfer Endpoint for Carscan Vehicle Data Ingestion"

**Column: Workload Description (Max 50 words):**

* **Sentence 1 (Project Context):** Start with a starter from Section 6. State what the customer is **building/architecting/launching**. **Include Region** here.
  
* **Sentence 2 (Technical Architecture):** Describe the technical approach using low-level specifics. **Reference OS/Instance Type/Database Engine** where relevant.
  * Examples: "The architecture deploys PostgreSQL on db.t4g.micro burstable instances," "The solution uses Linux-based r5.2xlarge instances for in-memory caching," "The design incorporates Windows Server m5.large nodes for application tier"
  
* **Sentence 3 (Business Outcome):** Conclude with the new business capability this enables.
  * Examples: "...enabling data persistence for production clusters," "...facilitating secure outbound connectivity for private subnets," "...supporting DAG scheduling and task execution tracking," "...enabling real-time event stream archival"

* **Net New Workload Guidelines:**
  * Use project/building language ("is building," "is architecting," "is launching")
  * Emphasize **new capabilities being created**
  * Focus on **what's being constructed** and **why it's needed**

---

#### **6. Mandatory Variation (Description Starters - Net New Focus):**
*Rotate these for each unique group:*

* `The customer is building a [Component] for [Clean System Name] in [Region] to...`
* `The project architects [Clean System Name] in [Region] using...`
* `The customer is launching [Clean System Name] operations in [Region] by deploying...`
* `New workload development for [Clean System Name] includes [Instance Type/OS] in [Region] to...`
* `The customer is establishing [Component] for [Clean System Name] in [Region] to...`
* `The architecture for [Clean System Name] in [Region] deploys...`

---

### **Example Net New Output:**

| Workload Title | Workload Description |
|----------------|----------------------|
| **Build MongoDB-Compatible Multi-Instance Cluster for Beem Project** | The customer is building a document database for Beem Project in US East (N. Virginia) to support new application requirements. The architecture deploys Amazon DocumentDB in a multi-instance cluster configuration to provide MongoDB-compatible operations with schema parity. This enables persistent document storage and query processing for the new production application tier. |
| **Architect ML Model Training Environment for Branch Analytics** | The project architects Branch Analytics in Asia Pacific (Mumbai) using Amazon SageMaker Studio workspace environments. The solution allocates isolated compute resources for model experimentation and notebook-based development workflows. This enables iterative training and validation for new machine learning capabilities. |
| **Launch Workflow Orchestration Backend for Branch Airflow Staging** | The customer is launching Branch Airflow operations in Asia Pacific (Mumbai) by deploying PostgreSQL on db.t4g.micro burstable instances. The database stores workflow metadata, DAG scheduling state, and task execution logs. This facilitates orchestration management for new staging environment data pipelines. |

---

**Input Data**