-- ============================================================
-- Seed data for CPT202 Project Selection System
-- All accounts use the same password as existing test accounts
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE notifications;
TRUNCATE TABLE conflict_logs;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE verification_codes;
TRUNCATE TABLE password_reset_tokens;
TRUNCATE TABLE applications;
TRUNCATE TABLE project_topics;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- CATEGORIES (10)
-- ============================================================
INSERT INTO categories (id, name, description, is_active, created_at) VALUES
(1,  'Machine Learning', 'Machine learning research including deep learning, NLP, computer vision and reinforcement learning', 1, '2026-01-10 09:00:00'),
(2,  'Web & Cloud Computing',                      'Full-stack web development, cloud-native architectures, microservices and DevOps',       1, '2026-01-10 09:00:00'),
(3,  'Data Science & Analytics',                   'Big data processing, statistical modelling, visualisation and business intelligence',    1, '2026-01-10 09:00:00'),
(4,  'Cybersecurity & Network Security',           'Threat detection, cryptography, secure systems design and privacy-enhancing tech',      1, '2026-01-10 09:00:00'),
(5,  'Mobile & Embedded Systems',                  'iOS/Android development, embedded firmware, IoT devices and edge computing',            1, '2026-01-10 09:00:00'),
(6,  'Augmented & Virtual Reality',                'AR/VR application development, spatial computing and immersive experience design',      1, '2026-01-10 09:00:00'),
(7,  'Robotics & Autonomous Systems',              'Robot perception, motion planning, autonomous vehicles and multi-robot coordination',   1, '2026-01-10 09:00:00'),
(8,  'Bioinformatics & Healthcare IT',             'Genomic data analysis, clinical decision support, medical imaging and health informatics', 1, '2026-01-10 09:00:00'),
(9,  'Sustainable Technology',                     'Green computing, smart grids, energy optimisation and environmental monitoring',        1, '2026-01-10 09:00:00'),
(10, 'Human-Computer Interaction',                 'Usability research, accessibility, UX design and adaptive interfaces',                  1, '2026-01-10 09:00:00');

-- ============================================================
-- USERS — Admin (1) + Teachers (5) + Students (10)
-- Password hash reused from existing test accounts
-- ============================================================
INSERT INTO users (id, email, password_hash, name, role, phone, department, status, created_at) VALUES
-- Admin
(1,  'admin@xjtlu.edu.cn',                   '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'System Administrator',  'admin',   NULL,          'IT Services',                   'active', '2026-01-08 08:00:00'),
-- Teachers
(2,  'james.harrison@xjtlu.edu.cn',          '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Dr. James Harrison',    'teacher', '+86-512-8816-1201', 'Computer Science & Technology', 'active', '2026-01-10 09:00:00'),
(3,  'sarah.chen@xjtlu.edu.cn',              '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Dr. Sarah Chen',        'teacher', '+86-512-8816-1342', 'Data Science',                  'active', '2026-01-10 09:00:00'),
(4,  'michael.wong@xjtlu.edu.cn',            '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Prof. Michael Wong',    'teacher', '+86-512-8816-1455', 'Cybersecurity',                 'active', '2026-01-10 09:00:00'),
(5,  'emily.roberts@xjtlu.edu.cn',           '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Dr. Emily Roberts',     'teacher', '+86-512-8816-1567', 'Software Engineering',          'active', '2026-01-10 09:00:00'),
(6,  'david.liu@xjtlu.edu.cn',               '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Prof. David Liu',       'teacher', '+86-512-8816-1678', 'Electrical & Electronic Eng.',  'active', '2026-01-10 09:00:00'),
-- Students
(7,  'alex.thompson@student.xjtlu.edu.cn',   '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Alex Thompson',         'student', '+86-151-0001-0001', 'Computer Science',              'active', '2026-02-01 10:00:00'),
(8,  'bella.zhang@student.xjtlu.edu.cn',     '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Bella Zhang',           'student', '+86-151-0001-0002', 'Data Science',                  'active', '2026-02-01 10:00:00'),
(9,  'chris.park@student.xjtlu.edu.cn',      '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Chris Park',            'student', '+86-151-0001-0003', 'Computer Science',              'active', '2026-02-01 10:00:00'),
(10, 'diana.mueller@student.xjtlu.edu.cn',   '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Diana Mueller',         'student', '+86-151-0001-0004', 'Software Engineering',          'active', '2026-02-01 10:00:00'),
(11, 'ethan.brown@student.xjtlu.edu.cn',     '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Ethan Brown',           'student', '+86-151-0001-0005', 'Electrical Engineering',        'active', '2026-02-01 10:00:00'),
(12, 'fiona.liu@student.xjtlu.edu.cn',       '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Fiona Liu',             'student', '+86-151-0001-0006', 'Data Science',                  'active', '2026-02-01 10:00:00'),
(13, 'george.smith@student.xjtlu.edu.cn',    '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'George Smith',          'student', '+86-151-0001-0007', 'Computer Science',              'active', '2026-02-01 10:00:00'),
(14, 'hannah.kim@student.xjtlu.edu.cn',      '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Hannah Kim',            'student', '+86-151-0001-0008', 'Software Engineering',          'active', '2026-02-01 10:00:00'),
(15, 'ian.foster@student.xjtlu.edu.cn',      '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Ian Foster',            'student', '+86-151-0001-0009', 'Cybersecurity',                 'active', '2026-02-01 10:00:00'),
(16, 'julia.wang@student.xjtlu.edu.cn',      '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Julia Wang',            'student', '+86-151-0001-0010', 'Computer Science',              'active', '2026-02-01 10:00:00');

-- ============================================================
-- PROJECT TOPICS (50)
-- teacher_id: Harrison=2, Chen=3, Wong=4, Roberts=5, Liu=6
-- status: unpublished / available / requested / agreed / closed / archived
-- ============================================================
INSERT INTO project_topics (id, teacher_id, category_id, title, description, required_skills, keywords, max_students, is_draft, status, created_at, updated_at) VALUES

-- === Dr. James Harrison (Machine Learning) === --
(1,  2, 1, 'Deep Learning for Medical Image Segmentation',
 'This project investigates the use of convolutional neural networks and transformer-based architectures for semantic segmentation of MRI and CT scan images. The goal is to automate the delineation of anatomical structures and tumour boundaries, reducing radiologist workload and improving diagnostic consistency. Students will work with public medical imaging datasets and implement state-of-the-art segmentation models including U-Net, nnU-Net, and Swin-UNETR.',
 'Python, PyTorch or TensorFlow, basic understanding of CNNs, familiarity with medical imaging (desirable)',
 'deep learning, medical imaging, segmentation, CNN, transformer', 2, 0, 'available', '2026-02-10 09:00:00', '2026-02-10 09:00:00'),

(2,  2, 1, 'Explainable Machine Learning for Credit Risk Assessment',
 'Financial institutions increasingly rely on black-box machine learning models for credit scoring, raising concerns about fairness and regulatory compliance. This project develops explainability methods (SHAP, LIME, attention mechanisms) applied to gradient-boosted tree models and neural networks trained on credit risk datasets. The outcome will be an interpretable dashboard that highlights key risk factors for individual loan decisions.',
 'Python, scikit-learn, XGBoost, basic statistics, interest in fintech',
 'explainable machine learning, model interpretability, credit risk, SHAP, fairness', 1, 0, 'available', '2026-02-12 09:00:00', '2026-02-12 09:00:00'),

(3,  2, 1, 'Federated Learning for Privacy-Preserving Healthcare Data Analysis',
 'Centralising patient data across hospitals raises serious privacy and legal barriers. This project implements a federated learning framework that trains shared models across distributed hospital nodes without raw data leaving each site. Students will implement FedAvg and differential-privacy variants, evaluate convergence on disease prediction tasks, and benchmark against centralised baselines.',
 'Python, PyTorch, basic understanding of distributed systems, knowledge of differential privacy (desirable)',
 'federated learning, privacy, healthcare, distributed ML', 2, 0, 'requested', '2026-02-15 09:00:00', '2026-03-01 10:00:00'),

(4,  2, 1, 'Natural Language Processing for Automated Essay Scoring',
 'Automated essay scoring (AES) systems can provide immediate feedback to students at scale. This project builds a transformer-based AES pipeline using BERT and RoBERTa fine-tuned on the ASAP dataset. Beyond holistic scoring, the system will provide trait-level feedback on coherence, grammar, and argumentation. A web-based student-facing interface will be prototyped.',
 'Python, HuggingFace Transformers, NLP fundamentals, basic web development',
 'NLP, essay scoring, BERT, education technology, transformer', 1, 0, 'agreed', '2026-02-18 09:00:00', '2026-03-15 10:00:00'),

(5,  2, 7, 'Reinforcement Learning for Autonomous Traffic Signal Control',
 'Urban traffic congestion costs billions annually. This project trains deep reinforcement learning agents (PPO, SAC) to control traffic signal timings across multi-intersection road networks using the SUMO traffic simulator. The agent learns to minimise average vehicle waiting time and CO2 emissions. Students will compare centralised, decentralised, and multi-agent approaches.',
 'Python, reinforcement learning basics, experience with simulation environments',
 'reinforcement learning, traffic control, autonomous systems, multi-agent', 2, 0, 'available', '2026-02-20 09:00:00', '2026-02-20 09:00:00'),

(6,  2, 1, 'Generative Adversarial Networks for Synthetic Training Data Augmentation',
 'Scarcity of labelled data is a persistent bottleneck in supervised learning. This project explores conditional GANs and diffusion models for synthesising realistic training images across domains including medical imaging, autonomous driving, and defect detection. Students will evaluate whether synthetic data improves downstream classifier performance and study mode collapse mitigation strategies.',
 'Python, PyTorch, experience with CNNs and GANs, familiarity with image datasets',
 'GAN, data augmentation, synthetic data, diffusion model', 2, 0, 'available', '2026-02-22 09:00:00', '2026-02-22 09:00:00'),

(7,  2, 1, 'Fine-Tuning Large Language Models for Domain-Specific Question Answering',
 'General-purpose LLMs often underperform on specialised domains such as law, medicine, and engineering. This project implements parameter-efficient fine-tuning (LoRA, QLoRA) of open-source LLMs (LLaMA-3, Mistral) on domain-specific corpora, evaluates retrieval-augmented generation (RAG) pipelines, and benchmarks outputs on expert-curated QA test sets.',
 'Python, HuggingFace, familiarity with LLMs, GPU access preferred',
 'LLM, fine-tuning, LoRA, RAG, question answering', 1, 0, 'requested', '2026-02-25 09:00:00', '2026-03-10 10:00:00'),

(8,  2, 1, 'Computer Vision for Real-Time Product Defect Detection in Manufacturing',
 'Industrial quality control still relies heavily on manual visual inspection, which is slow and error-prone. This project develops a real-time defect detection system using YOLO-v8 and EfficientDet trained on publicly available industrial defect datasets (MVTec AD, NEU). Students will deploy the model on a simulated camera feed and assess precision-recall trade-offs for different defect categories.',
 'Python, PyTorch, OpenCV, understanding of object detection',
 'computer vision, defect detection, YOLO, manufacturing, real-time', 2, 0, 'available', '2026-02-28 09:00:00', '2026-02-28 09:00:00'),

(9,  2, 3, 'Multi-Modal Sentiment Analysis for Social Media Brand Monitoring',
 'Brand perception on social media is shaped by both text and visual content. This project builds a multi-modal sentiment analysis system that fuses textual embeddings (BERT) with visual features (ViT) extracted from tweet images. Students will collect and annotate a brand-specific dataset, train fusion architectures, and build a real-time monitoring dashboard.',
 'Python, NLP, basic computer vision, interest in social media analytics',
 'sentiment analysis, multi-modal, social media, brand monitoring', 2, 0, 'available', '2026-03-01 09:00:00', '2026-03-01 09:00:00'),

(10, 2, 1, 'Anomaly Detection in Industrial IoT Sensor Streams',
 'Early fault detection in industrial machinery can prevent costly downtime. This project applies unsupervised and semi-supervised anomaly detection methods — including Isolation Forest, Autoencoder, and Temporal Convolutional Networks — to multivariate time-series data from industrial sensors (SWAT, BATADAL datasets). A streaming pipeline prototype using Apache Kafka will be implemented.',
 'Python, time series analysis, basic ML, interest in IoT',
 'anomaly detection, IoT, time series, autoencoder, industrial', 2, 0, 'archived', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),

-- === Dr. Sarah Chen (Data Science) === --
(11, 3, 3, 'Predictive Analytics for Student Academic Performance and Early Intervention',
 'Universities struggle to identify at-risk students early enough to provide effective support. This project builds a predictive model using features from the VLE interaction logs, assessment results, and demographic data from the Open University Learning Analytics Dataset. Students will develop and evaluate logistic regression, random forest, and gradient boosting models, and design an intervention recommendation interface for academic advisors.',
 'Python, pandas, scikit-learn, basic statistics, interest in education data',
 'learning analytics, prediction, student success, early warning, education', 2, 0, 'available', '2026-02-10 09:30:00', '2026-02-10 09:30:00'),

(12, 3, 3, 'Time Series Forecasting for Municipal Energy Demand Prediction',
 'Accurate energy demand forecasting enables utilities to optimise generation planning and reduce reliance on expensive peaker plants. This project develops multi-step-ahead forecasting models including SARIMA, Prophet, and LSTM networks applied to hourly smart meter data from a European city. Students will investigate the impact of weather covariates, public holidays, and price signals on model accuracy.',
 'Python, pandas, time series, statistics, interest in energy systems',
 'time series, forecasting, energy, LSTM, smart grid', 1, 0, 'available', '2026-02-12 09:30:00', '2026-02-12 09:30:00'),

(13, 3, 3, 'Graph Neural Networks for Social Network Analysis and Community Detection',
 'Online social networks exhibit complex community structures that influence information diffusion and opinion formation. This project applies graph neural networks (GraphSAGE, GAT, GCN) to tasks including community detection, link prediction, and influence maximisation on real-world social network datasets. Students will compare GNN approaches against classical spectral methods.',
 'Python, PyTorch Geometric or DGL, understanding of graph theory and ML',
 'graph neural network, social network, community detection, GNN', 2, 0, 'requested', '2026-02-14 09:30:00', '2026-03-05 10:00:00'),

(14, 3, 3, 'Big Data Pipeline for Real-Time Financial Market Risk Assessment',
 'Financial risk teams require low-latency pipelines to compute Value-at-Risk and portfolio exposure metrics from streaming market data. This project designs and implements a big data pipeline using Apache Spark Streaming and Kafka, computes risk metrics in near-real-time, and stores results in a columnar data warehouse. Students will benchmark latency and throughput under simulated market stress conditions.',
 'Spark, Kafka, Python or Scala, basic understanding of financial risk metrics',
 'big data, Kafka, Spark, financial risk, streaming', 1, 0, 'agreed', '2026-02-16 09:30:00', '2026-03-20 10:00:00'),

(15, 3, 2, 'Cloud Data Warehouse Modernisation Using Lakehouse Architecture',
 'Legacy data warehouses struggle to handle unstructured data and real-time analytics at cloud scale. This project designs a lakehouse architecture using Apache Iceberg on AWS S3, implements incremental data ingestion pipelines, and benchmarks query performance against a traditional star-schema warehouse. Students will produce a migration playbook applicable to a mid-sized enterprise.',
 'SQL, Python, basic cloud services (AWS or Azure), interest in data engineering',
 'data warehouse, lakehouse, Iceberg, cloud, data engineering', 2, 0, 'available', '2026-02-18 09:30:00', '2026-02-18 09:30:00'),

(16, 3, 3, 'Visual Analytics Dashboard for Global Supply Chain Risk Monitoring',
 'Supply chain disruptions — from geopolitical events to natural disasters — are difficult to anticipate. This project builds an interactive analytics dashboard (using D3.js or Plotly Dash) that aggregates news sentiment, logistics data, and macroeconomic indicators to surface emerging supply chain risks. Students will design an alerting layer and a risk scoring methodology.',
 'Python, data visualisation, basic NLP, interest in logistics',
 'supply chain, dashboard, visualisation, risk, analytics', 2, 0, 'available', '2026-02-20 09:30:00', '2026-02-20 09:30:00'),

(17, 3, 3, 'Knowledge Graph Construction from Scientific Literature',
 'Scientific knowledge is locked in millions of unstructured papers. This project builds an automated pipeline that extracts entities and relations from biomedical abstracts using named-entity recognition and relation extraction models, populates a Neo4j knowledge graph, and exposes a SPARQL-compatible query interface. Students will evaluate the knowledge graph against a gold-standard subset.',
 'Python, NLP, basic graph databases, interest in information extraction',
 'knowledge graph, NLP, information extraction, Neo4j, biomedical', 1, 0, 'available', '2026-02-22 09:30:00', '2026-02-22 09:30:00'),

(18, 3, 3, 'Streaming Analytics Platform for Smart-Building IoT Sensor Data',
 'Modern office buildings generate continuous streams of environmental sensor data (temperature, occupancy, air quality). This project implements a streaming analytics platform using Flink or Spark Structured Streaming, computes aggregate KPIs, detects anomalies, and feeds a live Grafana dashboard. Students will evaluate at-least-once vs exactly-once processing semantics.',
 'Python or Java, streaming processing basics, interest in IoT and smart buildings',
 'streaming, IoT, Flink, Grafana, smart building', 2, 0, 'requested', '2026-02-24 09:30:00', '2026-03-08 10:00:00'),

(19, 3, 3, 'Causal Inference Methods for A/B Testing in E-Commerce',
 'Standard A/B tests are biased when users are networked or when interference occurs. This project implements and compares causal inference approaches — propensity score matching, difference-in-differences, and synthetic control — applied to simulated e-commerce experiments. Students will develop a framework for recommending the appropriate causal method given experimental constraints.',
 'Python, statistics (regression, hypothesis testing), interest in causal inference',
 'causal inference, A/B testing, e-commerce, statistics', 1, 0, 'available', '2026-02-26 09:30:00', '2026-02-26 09:30:00'),

(20, 3, 1, 'MLOps Platform for Automated Machine Learning Pipeline Management',
 'Deploying and monitoring ML models in production remains a significant engineering challenge. This project builds an end-to-end MLOps platform integrating MLflow for experiment tracking, Airflow for pipeline orchestration, and Prometheus/Grafana for model monitoring. Students will deploy a sample classification model and implement automated retraining triggers based on data drift detection.',
 'Python, Docker, basic CI/CD, familiarity with ML frameworks',
 'MLOps, MLflow, Airflow, model monitoring, pipeline', 2, 0, 'closed', '2026-01-20 09:30:00', '2026-04-01 10:00:00'),

-- === Prof. Michael Wong (Cybersecurity) === --
(21, 4, 4, 'Zero-Trust Architecture Implementation for Enterprise Network Security',
 'The traditional perimeter-based security model is inadequate for modern cloud-hybrid enterprises. This project designs and evaluates a zero-trust architecture prototype using identity-aware proxies, micro-segmentation, and continuous verification policies. Students will simulate common attack vectors (lateral movement, credential theft) and measure the efficacy of zero-trust controls in a virtual lab environment.',
 'Networking fundamentals, Linux, basic understanding of IAM, interest in enterprise security',
 'zero trust, network security, IAM, micro-segmentation, enterprise', 2, 0, 'available', '2026-02-10 10:00:00', '2026-02-10 10:00:00'),

(22, 4, 4, 'Blockchain-Based Decentralised Identity Verification System',
 'Centralised identity systems create single points of failure and privacy risks. This project implements a self-sovereign identity system on Ethereum using Verifiable Credentials (W3C standard) and decentralised identifiers (DIDs). Students will build a demo application for university credential issuance and verification, and analyse gas costs and scalability under load.',
 'Solidity, Ethereum/Web3, understanding of PKI and cryptography',
 'blockchain, decentralised identity, DID, Ethereum, self-sovereign identity', 1, 0, 'available', '2026-02-12 10:00:00', '2026-02-12 10:00:00'),

(23, 4, 4, 'Deep Learning-Based Intrusion Detection for Encrypted Network Traffic',
 'Encrypting network traffic protects privacy but also blinds traditional signature-based IDS. This project trains deep learning classifiers on traffic flow features (packet sizes, inter-arrival times, byte distributions) extracted from the CICIDS-2018 dataset to detect malware communication, DDoS attacks, and data exfiltration without decrypting payloads.',
 'Python, machine learning, Wireshark or similar, basic networking',
 'intrusion detection, encrypted traffic, deep learning, network security', 2, 0, 'requested', '2026-02-14 10:00:00', '2026-03-03 10:00:00'),

(24, 4, 4, 'Secure Multi-Party Computation for Privacy-Preserving Collaborative Analytics',
 'Organisations often wish to jointly compute analytics over combined datasets without revealing individual records. This project implements secure multi-party computation protocols (GMW, SPDZ) using the MP-SPDZ framework for logistic regression and aggregation queries, benchmarks communication and computation overhead, and evaluates practical applicability in a healthcare cost-sharing scenario.',
 'Python, cryptography fundamentals, linear algebra, interest in privacy-enhancing technologies',
 'secure computation, MPC, privacy, cryptography, collaborative analytics', 1, 0, 'agreed', '2026-02-16 10:00:00', '2026-03-18 10:00:00'),

(25, 4, 4, 'Formal Verification of Smart Contract Security Properties',
 'Smart contract vulnerabilities have resulted in hundreds of millions of dollars in losses. This project applies formal verification tools (Certora Prover, Slither, Echidna) to identify reentrancy, integer overflow, and access-control vulnerabilities in sample DeFi smart contracts. Students will also develop a lightweight specification language for expressing common security invariants.',
 'Solidity, basic understanding of formal methods, interest in blockchain security',
 'smart contract, formal verification, security, Solidity, DeFi', 2, 0, 'available', '2026-02-18 10:00:00', '2026-02-18 10:00:00'),

(26, 4, 4, 'Adversarial Robustness Evaluation of Image Classification Models',
 'Deep neural networks are highly vulnerable to adversarial perturbations invisible to human observers. This project systematically evaluates the robustness of ImageNet classifiers against white-box (PGD, C&W) and black-box (HopSkipJump, Square Attack) attacks, and implements adversarial training and certified defences (randomised smoothing). Students will produce a robustness benchmark report.',
 'Python, PyTorch, understanding of CNNs and optimisation',
 'adversarial examples, robustness, security, deep learning, certification', 2, 0, 'available', '2026-02-20 10:00:00', '2026-02-20 10:00:00'),

(27, 4, 4, 'Differential Privacy Mechanisms for Federated Analytics',
 'Publishing aggregate statistics from sensitive datasets can leak private information through differencing attacks. This project implements and evaluates local and central differential privacy mechanisms — Laplace, Gaussian, and Randomised Response — applied to a federated analytics scenario with healthcare and financial datasets. Students will analyse the utility-privacy trade-off under varying epsilon budgets.',
 'Python, statistics, basic cryptography, interest in privacy',
 'differential privacy, federated analytics, privacy, statistics', 2, 0, 'requested', '2026-02-22 10:00:00', '2026-03-12 10:00:00'),

(28, 4, 4, 'Automated Penetration Testing Platform',
 'Manual penetration testing is expensive and hard to scale. This project develops a pentest automation framework that uses reinforcement learning to guide vulnerability scanning, exploit selection, and post-exploitation actions in a controlled virtual lab (HackTheBox-style machines). Students will compare automated, random, and human-guided approaches on a standardised testbed.',
 'Linux, basic networking, Python, interest in offensive security and automation',
 'penetration testing, automation, reinforcement learning, offensive security', 2, 0, 'available', '2026-02-24 10:00:00', '2026-02-24 10:00:00'),

(29, 4, 4, 'Post-Quantum Cryptography: Implementation and Performance Benchmarking',
 'NIST has standardised four post-quantum cryptographic algorithms (CRYSTALS-Kyber, CRYSTALS-Dilithium, FALCON, SPHINCS+). This project implements these algorithms in Python and C, integrates them into a TLS 1.3 handshake prototype, and benchmarks key generation, encapsulation, and signing performance against classical RSA and ECDSA on both desktop and embedded platforms.',
 'C/C++ or Python, understanding of public-key cryptography, interest in cryptographic standards',
 'post-quantum cryptography, NIST, Kyber, Dilithium, TLS', 1, 0, 'available', '2026-02-26 10:00:00', '2026-02-26 10:00:00'),

(30, 4, 4, 'Cyber Threat Intelligence Platform Using Honeypot Networks',
 'Honeypots attract adversaries and yield valuable threat intelligence about attack techniques and tooling. This project deploys a network of medium-interaction honeypots (Cowrie, Dionaea) in a cloud environment, ingests attack logs into an ELK stack, applies clustering to identify attacker campaigns, and produces structured threat intelligence feeds in STIX/TAXII format.',
 'Linux, basic networking, Docker, interest in threat intelligence',
 'honeypot, threat intelligence, STIX, security operations, ELK', 2, 0, 'archived', '2026-01-18 10:00:00', '2026-01-18 10:00:00'),

-- === Dr. Emily Roberts (HCI / Web / AR) === --
(31, 5, 10, 'Accessible Web Application Design: Automated Compliance and User Testing',
 'Approximately 15% of the global population lives with a disability, yet most websites fail basic accessibility standards. This project builds an automated accessibility auditing tool (extending axe-core) integrated with a screen-reader user study, identifies gaps in WCAG 2.2 compliance across a sample of university websites, and produces design recommendations and code fixes.',
 'HTML/CSS/JavaScript, basic UX research methods, empathy for diverse users',
 'accessibility, WCAG, screen reader, UX, inclusive design', 2, 0, 'available', '2026-02-10 10:30:00', '2026-02-10 10:30:00'),

(32, 5, 2, 'Progressive Web App for Decentralised Campus Resource Booking',
 'University resource booking systems are often fragmented, slow, and mobile-unfriendly. This project builds a Progressive Web App using React and Service Workers that enables offline booking, push notifications, and seamless multi-device synchronisation, backed by a Spring Boot REST API and a PostgreSQL database. QR-code-based room check-in will be implemented.',
 'React, JavaScript, REST APIs, basic backend development',
 'PWA, React, Service Workers, campus management, mobile-first', 2, 0, 'available', '2026-02-12 10:30:00', '2026-02-12 10:30:00'),

(33, 5, 10, 'Cognitive Load in Code Suggestion Editors: A Mixed-Methods Evaluation',
 'Code suggestion tools promise productivity gains but may also increase cognitive load for novice developers. This project conducts a mixed-methods study combining eye-tracking, EEG-based cognitive load measurement, and think-aloud protocols to evaluate how different suggestion styles affect novice and expert programmers. Findings will inform design guidelines.',
 'Interest in HCI research methods, basic programming experience, willingness to conduct user studies',
 'cognitive load, code suggestion, eye-tracking, HCI, developer tools', 2, 0, 'requested', '2026-02-14 10:30:00', '2026-03-06 10:00:00'),

(34, 5, 10, 'Affective Computing Interface for Student Mental Health Monitoring',
 'Student mental health crises are often detected too late. This project develops a passive affect recognition system that analyses facial action units (OpenFace), voice prosody, and typing dynamics to infer student stress levels during study sessions. A privacy-preserving on-device processing architecture will ensure data never leaves the student device. An alert system for welfare officers will be prototyped.',
 'Python, OpenCV or MediaPipe, interest in affective computing and ethics of surveillance',
 'affective computing, mental health, privacy, emotion recognition, well-being', 1, 0, 'agreed', '2026-02-16 10:30:00', '2026-03-22 10:00:00'),

(35, 5, 2, 'Serverless Microservices Architecture for Event-Driven E-Learning Platform',
 'Monolithic LMS platforms struggle to scale during peak assessment periods. This project designs and prototypes an event-driven e-learning platform using AWS Lambda, API Gateway, EventBridge, and DynamoDB. Students will implement key modules (quiz engine, progress tracking, notification service) as stateless functions, demonstrate auto-scaling behaviour, and compare costs with EC2-based equivalents.',
 'AWS or equivalent cloud platform, basic backend development, understanding of REST APIs',
 'serverless, AWS Lambda, microservices, e-learning, event-driven', 2, 0, 'available', '2026-02-18 10:30:00', '2026-02-18 10:30:00'),

(36, 5, 10, 'Design System Development for University Digital Services Unification',
 'XJTLU digital services span dozens of web and mobile applications with inconsistent UI. This project audits existing applications, derives a cohesive design system (tokens, component library in React and Figma), implements accessibility and dark-mode support, and produces documentation and a governance process for adoption. The design system will be validated with student and staff participants.',
 'React, CSS, Figma or equivalent, interest in design systems and component libraries',
 'design system, UI components, React, accessibility, Figma', 2, 0, 'available', '2026-02-20 10:30:00', '2026-02-20 10:30:00'),

(37, 5, 6, 'Augmented Reality Indoor Navigation for University Campus Accessibility',
 'Navigating large university campuses is challenging for new students and visitors with mobility impairments. This project develops an AR indoor navigation app using ARKit/ARCore, integrated with a BLE beacon network for sub-metre positioning. Route planning algorithms will optimise for accessibility constraints (step-free routes, lift locations). Usability testing with wheelchair users will be conducted.',
 'iOS (Swift) or Android (Kotlin), basic AR development, interest in accessibility',
 'augmented reality, indoor navigation, accessibility, ARKit, ARCore', 2, 0, 'requested', '2026-02-22 10:30:00', '2026-03-14 10:00:00'),

(38, 5, 10, 'Adaptive Learning Interface Using Real-Time Cognitive Load Estimation',
 'One-size-fits-all e-learning fails to accommodate learner variability. This project builds an adaptive learning interface that uses real-time pupil dilation and task performance metrics to estimate cognitive load and dynamically adjusts content presentation (chunk size, hint availability, difficulty level). Students will evaluate learning outcomes against a static interface in a controlled study.',
 'JavaScript/React, basic ML inference (TensorFlow.js), interest in adaptive systems',
 'adaptive learning, cognitive load, pupil dilation, e-learning, personalisation', 1, 0, 'available', '2026-02-24 10:30:00', '2026-02-24 10:30:00'),

(39, 5, 5, 'Cross-Platform Mobile App for Student Wellbeing and Peer Support',
 'Peer support networks significantly improve student wellbeing outcomes. This project builds a cross-platform mobile app (React Native or Flutter) that facilitates anonymous peer support matching, mood journalling, and wellbeing resource discovery. A moderation layer using text classification will flag crisis indicators to trained welfare officers while preserving user anonymity.',
 'React Native or Flutter, basic REST API development, interest in mental health technology',
 'mobile app, wellbeing, peer support, React Native, Flutter', 2, 0, 'available', '2026-02-26 10:30:00', '2026-02-26 10:30:00'),

(40, 5, 10, 'Collaborative Filtering for Personalised Academic Resource Recommendation',
 'Students often struggle to discover relevant academic resources from vast institutional repositories. This project implements collaborative and content-based filtering algorithms for recommending journal articles, lecture notes, and tutorial videos based on a student learning profile. A hybrid recommender will be evaluated against baseline approaches on a synthetic interaction log.',
 'Python, machine learning, interest in recommender systems',
 'recommender system, collaborative filtering, personalisation, education', 2, 0, 'closed', '2026-01-22 10:30:00', '2026-03-30 10:00:00'),

-- === Prof. David Liu (Robotics / Embedded / Sustainable) === --
(41, 6, 7, 'Autonomous Drone Navigation in GPS-Denied Indoor Environments',
 'GPS is unavailable indoors, making autonomous drone navigation a hard open problem. This project implements a visual-inertial odometry (VIO) system on a DJI Tello drone, fuses LiDAR-lite range data with camera-based SLAM, and demonstrates fully autonomous navigation through a structured indoor obstacle course. The project will evaluate robustness to lighting changes and dynamic obstacles.',
 'Python, ROS 2, basic control systems, interest in aerial robotics',
 'autonomous drone, SLAM, indoor navigation, ROS, VIO', 2, 0, 'available', '2026-02-10 11:00:00', '2026-02-10 11:00:00'),

(42, 6, 7, 'Robot-Assisted Surgery Simulation and Skill Assessment Platform',
 'Surgical robotics training requires expensive physical simulators. This project builds a virtual simulation environment in Unity3D that models the da Vinci surgical robot kinematics, implements haptic feedback via a Phantom Omni device, and develops a skill assessment module that scores trainee performance on standardised laparoscopic tasks.',
 'C# (Unity), basic robotics kinematics, interest in medical robotics and simulation',
 'surgical robotics, simulation, Unity3D, haptic feedback, skill assessment', 2, 0, 'available', '2026-02-12 11:00:00', '2026-02-12 11:00:00'),

(43, 6, 5, 'Embedded Vision System for Automated PCB Quality Control',
 'Printed circuit board (PCB) defects cost the electronics industry billions annually. This project develops an embedded vision system on a Raspberry Pi 5 with a 12MP HQ camera that detects solder bridges, missing components, and misaligned ICs using a custom-trained MobileNetV3 classifier. A REST API will expose inspection results to a factory MES system.',
 'Python, embedded Linux, OpenCV, basic electronics knowledge',
 'embedded systems, computer vision, PCB inspection, quality control, Raspberry Pi', 1, 0, 'requested', '2026-02-14 11:00:00', '2026-03-04 10:00:00'),

(44, 6, 7, 'Decentralised Task Allocation for Multi-Robot Warehouse Automation',
 'Warehouse automation fleets must efficiently allocate tasks (pick, transport, charge) under dynamic demand and robot availability. This project implements and compares centralised (auction-based) and decentralised (CBBA algorithm) task allocation strategies in the CoppeliaSim multi-robot simulator, evaluates throughput and fairness metrics, and tests robustness to robot failures.',
 'Python, ROS 2, interest in multi-robot systems and combinatorial optimisation',
 'multi-robot, task allocation, warehouse automation, CBBA, ROS', 1, 0, 'agreed', '2026-02-16 11:00:00', '2026-03-24 10:00:00'),

(45, 6, 5, 'Edge Computing Framework for Real-Time Computer Vision Inference',
 'Cloud-based inference introduces latency and bandwidth costs unsuitable for real-time applications. This project designs and implements an edge inference framework using NVIDIA Jetson Orin Nano that supports model compilation with TensorRT, dynamic load balancing across multiple edge nodes, and seamless failover to the cloud. Inference latency and power consumption will be profiled.',
 'Python, C++, CUDA basics, interest in edge computing and deployment',
 'edge computing, TensorRT, Jetson, inference, computer vision', 2, 0, 'available', '2026-02-18 11:00:00', '2026-02-18 11:00:00'),

(46, 6, 9, 'Digital Twin Development for Predictive Maintenance in Smart Factories',
 'Digital twins enable manufacturers to simulate and predict equipment behaviour before physical failure occurs. This project develops a digital twin of a CNC milling machine using Siemens MindSphere APIs, synchronises sensor data in real time, trains a remaining-useful-life prediction model (LSTM), and visualises anomalies in a 3D Unity dashboard.',
 'Python, interest in IoT, basic control systems, familiarity with Unity3D (desirable)',
 'digital twin, predictive maintenance, smart factory, LSTM, IoT', 2, 0, 'available', '2026-02-20 11:00:00', '2026-02-20 11:00:00'),

(47, 6, 7, 'Safety Monitoring System for Human-Robot Collaboration Workspaces',
 'Collaborative robots (cobots) operating alongside humans require real-time safety monitoring to prevent collisions. This project implements a 3D human pose estimation system (MediaPipe BlazePose) fused with a cobot arm model to predict collision risks and trigger safety stops. A risk assessment framework will be developed in accordance with ISO/TS 15066 cobot safety standards.',
 'Python, OpenCV, ROS 2, interest in robot safety and human factors',
 'cobot, human-robot collaboration, safety, pose estimation, ISO 15066', 2, 0, 'requested', '2026-02-22 11:00:00', '2026-03-16 10:00:00'),

(48, 6, 5, 'On-Device Machine Learning for Wearable Health Monitoring',
 'Continuous health monitoring wearables are power-constrained and cannot rely on cloud connectivity. This project designs and trains compact ML models (TinyML) for heart rate variability, fall detection, and activity recognition using the PAMAP2 and PPG-DaLiA datasets, deploys them on an Arduino Nano 33 BLE Sense, and evaluates accuracy-power trade-offs using TensorFlow Lite Micro.',
 'Embedded C, Python, TensorFlow Lite, interest in wearables and health sensing',
 'TinyML, wearable, health monitoring, on-device ML, Arduino', 2, 0, 'available', '2026-02-24 11:00:00', '2026-02-24 11:00:00'),

(49, 6, 9, 'Smart Grid Energy Optimisation with Deep Reinforcement Learning',
 'Integrating intermittent renewable sources into the grid requires intelligent real-time control of storage and dispatch. This project trains a DRL agent (SAC, TD3) in the Pandapower smart grid simulation environment to optimise battery storage scheduling, minimise operating costs, and maintain voltage stability under stochastic solar and wind generation profiles.',
 'Python, reinforcement learning basics, interest in energy systems and sustainability',
 'smart grid, reinforcement learning, energy optimisation, renewable energy, battery storage', 1, 0, 'available', '2026-02-26 11:00:00', '2026-02-26 11:00:00'),

(50, 6, 7, 'Swarm Robotics for Environmental Pollutant Monitoring',
 'Measuring pollutant distributions across large areas with fixed sensors is expensive and imprecise. This project implements a swarm of simulated mobile robots (ARGoS simulator) that cooperatively map pollutant concentrations using stigmergy-based algorithms and adaptive sampling. Students will compare swarm efficiency against random and grid-based sampling strategies.',
 'Python, C++, interest in swarm intelligence and environmental sensing',
 'swarm robotics, environmental monitoring, stigmergy, ARGoS, cooperative sensing', 2, 0, 'archived', '2026-01-25 11:00:00', '2026-01-25 11:00:00');

-- ============================================================
-- APPLICATIONS
-- Realistic personal statements, varied statuses
-- ============================================================
INSERT INTO applications (id, student_id, project_id, personal_statement, status, created_at, updated_at) VALUES

-- Project 3 (Federated Learning — requested) — 2 pending
(1,  7,  3,
 'I am deeply interested in the intersection of machine learning and healthcare privacy, which is why this project resonates strongly with me. In my third year, I completed a self-directed study of differential privacy mechanisms and implemented a basic federated learning prototype using PyTorch and the Flower framework. I believe that federated approaches will be essential for regulated healthcare environments, and I am eager to work on real convergence challenges across non-IID data distributions.',
 'pending', '2026-03-01 14:22:00', '2026-03-01 14:22:00'),

(2,  12, 3,
 'During my data science programme I have developed strong Python and PyTorch skills, and I recently read the seminal FedAvg paper alongside several follow-up works on heterogeneous federated learning. I am particularly motivated by the challenge of applying these methods to medical imaging, where data siloing is a genuine clinical barrier. I have prior experience processing DICOM files and building binary classifiers on the NIH Chest X-Ray dataset, which I believe is highly relevant to this project.',
 'pending', '2026-03-03 09:15:00', '2026-03-03 09:15:00'),

-- Project 4 (NLP Essay Scoring — agreed) — 1 accepted, 1 rejected
(3,  8,  4,
 'Automated essay scoring sits at the intersection of my two core interests: NLP and education technology. I have completed Andrew Ng''s NLP Specialisation and independently fine-tuned a BERT model for sentiment classification on an Amazon review dataset, achieving 93.5% accuracy. My final-year plan is to pursue educational technology research, and this project would provide ideal grounding. I am comfortable with the HuggingFace ecosystem and have basic Flask experience for the web interface prototype.',
 'accepted', '2026-03-05 11:30:00', '2026-03-18 09:00:00'),

(4,  9,  4,
 'I applied for this project because of my background in linguistics and programming. However, after reflection, I recognise that Bella''s background in NLP and the HuggingFace toolkit makes her the stronger candidate for the available spot.',
 'rejected', '2026-03-06 16:45:00', '2026-03-18 09:05:00'),

-- Project 7 (LLM Fine-tuning — requested) — 1 pending
(5,  16, 7,
 'I have spent the past six months studying the LLM landscape, from the original GPT architecture through to the efficiency techniques introduced in LLaMA-2 and Mistral 7B. I have successfully run 4-bit quantised models locally using bitsandbytes and have experimented with simple LoRA fine-tuning on instruction datasets. I am particularly interested in the RAG component of this project — I believe the combination of retrieved context and domain fine-tuning is the most practical path to reliable domain-specific QA.',
 'pending', '2026-03-10 13:40:00', '2026-03-10 13:40:00'),

-- Project 13 (GNN Social Network — requested) — 2 pending
(6,  9,  13,
 'Graph neural networks are an area I have actively explored outside of the core curriculum. I completed Stanford''s CS224W course and implemented a GraphSAGE model from scratch to predict citation links in the Cora dataset. Community detection has interesting applications in understanding information echo chambers, which is a topic I find socially significant. I have strong Python and PyTorch Geometric skills and would welcome the opportunity to apply them to a real-world social network dataset.',
 'pending', '2026-03-05 10:20:00', '2026-03-05 10:20:00'),

(7,  11, 13,
 'I took a graph theory module in my second year and was immediately drawn to the computational challenges of large-scale graph analysis. I have since studied graph neural networks through blog posts, papers, and the DGL documentation. This project appeals to me because it bridges theoretical graph algorithms with practical social science questions. My relevant skills include Python, NetworkX, and a recent project applying Louvain community detection to a Twitter retweet network scraped using the v2 API.',
 'pending', '2026-03-07 15:55:00', '2026-03-07 15:55:00'),

-- Project 14 (Big Data Financial Risk — agreed) — 1 accepted
(8,  12, 14,
 'My career aspiration is to work in quantitative finance, and this project directly bridges my data engineering skills with that goal. I have practical experience with Apache Spark from a summer internship where I processed trade reconciliation data at a financial services firm in Shanghai. I am also familiar with Kafka from a university IoT project. I am confident in my ability to design and implement the streaming pipeline and would be enthusiastic about extending the project to incorporate ML-based risk factor estimation.',
 'accepted', '2026-03-08 09:00:00', '2026-03-22 08:30:00'),

-- Project 18 (Streaming Analytics — requested) — 2 pending
(9,  13, 18,
 'Smart building optimisation is an area I am passionate about, having worked on a building energy monitoring system for my second-year group project. I have hands-on experience with MQTT brokers, InfluxDB, and Grafana dashboards, which I believe maps closely to the platform described in this project. I am keen to extend my skills into Flink or Spark Structured Streaming, and I would particularly enjoy the challenge of implementing exactly-once processing semantics.',
 'pending', '2026-03-08 11:20:00', '2026-03-08 11:20:00'),

(10, 15, 18,
 'I have developed strong data engineering skills throughout my degree, including experience with Apache Kafka from a personal project and familiarity with time-series databases (InfluxDB, TimescaleDB). I am attracted to this project because it combines streaming data processing with real-world IoT sensor data in a built environment context. I would be happy to explore the exactly-once processing guarantees in Flink, which I have not yet had the opportunity to implement in practice.',
 'pending', '2026-03-10 14:35:00', '2026-03-10 14:35:00'),

-- Project 23 (Intrusion Detection — requested) — 2 pending
(11, 15, 23,
 'Network security is my primary area of study, and I have completed several Capture The Flag competitions (HTB, PicoCTF) where analysing network traffic was a core skill. I have built binary classifiers on the NSL-KDD dataset for a coursework assignment and am familiar with Scikit-learn, Keras, and Wireshark/tcpdump for traffic analysis. The challenge of detecting threats in encrypted flows, without decryption, is a genuinely hard problem that I would relish tackling with deep learning approaches.',
 'pending', '2026-03-03 16:10:00', '2026-03-03 16:10:00'),

(12, 9,  23,
 'I have a strong interest in security and have self-studied the CompTIA Security+ syllabus alongside my undergraduate programme. This project appeals to me because it applies machine learning to a real-world security problem that has immediate practical relevance. My relevant technical background includes Python for ML, basic Wireshark usage, and completion of TryHackMe''s learning path on network fundamentals and SOC analysis.',
 'pending', '2026-03-05 13:45:00', '2026-03-05 13:45:00'),

-- Project 24 (Secure MPC — agreed) — 1 accepted, 1 rejected
(13, 10, 24,
 'I took a graduate-level cryptography elective in my third year and was particularly fascinated by the section on secure multi-party computation. I have since read the foundational papers on GMW and SPDZ protocols and experimented with MP-SPDZ in a Docker environment. Applying MPC to healthcare analytics is something I believe has enormous real-world potential, and I would bring both the theoretical cryptographic background and the Python engineering skills required for this project.',
 'accepted', '2026-03-10 10:00:00', '2026-03-20 09:00:00'),

(14, 14, 24,
 'My motivation for applying is strong, but after speaking with Prof. Wong I acknowledge that Diana''s cryptographic background is a better fit for the theoretical depth required.',
 'rejected', '2026-03-11 11:20:00', '2026-03-20 09:05:00'),

-- Project 27 (Differential Privacy — requested) — 2 pending
(15, 10, 27,
 'Having studied the mathematical foundations of differential privacy in my cryptography module, I am eager to apply these concepts in a practical federated analytics setting. I understand the epsilon-delta privacy budget framework, the Laplace and Gaussian mechanisms, and the composition theorems. I also have Python experience with the Google DP library. I am particularly interested in exploring the utility-privacy trade-off on healthcare and financial datasets as described in the project brief.',
 'pending', '2026-03-12 10:30:00', '2026-03-12 10:30:00'),

(16, 16, 27,
 'I completed a module on privacy engineering last semester and wrote a literature review on differential privacy applications in federated learning. I am confident in the mathematical background required and have Python experience with data analysis libraries. I am keen to extend my theoretical understanding into a practical implementation, and the comparison of central vs local DP mechanisms on real datasets would be an ideal vehicle for this.',
 'pending', '2026-03-14 15:00:00', '2026-03-14 15:00:00'),

-- Project 33 (HCI code editors - requested) - 1 pending
(17, 14, 33,
 'I have a strong interest in human-computer interaction research, particularly the cognitive aspects of programmer experience. I completed a module on research methods and have basic experience with eye-tracking equipment from a lab demonstration session. I use code suggestion tools daily and have formed qualitative impressions about their effects on my own problem-solving, which I would like to examine rigorously. I am willing to recruit participants and manage the mixed-methods study protocol under Dr. Roberts'' supervision.',
 'pending', '2026-03-06 14:10:00', '2026-03-06 14:10:00'),

-- Project 34 (Affective Computing — agreed) — 1 accepted
(18, 8,  34,
 'Mental health technology is an area I am deeply committed to, having volunteered as a peer mental health supporter for two years. I have studied affective computing through academic papers and a Coursera specialisation on emotion recognition. My technical skills include Python, OpenCV for facial landmark detection, and librosa for audio feature extraction. I am especially attracted to the on-device processing architecture, which addresses the privacy concerns that I believe are central to the ethical deployment of this kind of system.',
 'accepted', '2026-03-12 09:30:00', '2026-03-25 08:45:00'),

-- Project 37 (AR Navigation — requested) — 2 pending
(19, 7,  37,
 'I developed an AR prototype for a hackathon project last year using ARKit on iOS and became fascinated by the challenges of robust marker-less tracking in real environments. Indoor navigation for accessibility is a cause I care about — my younger sister is a wheelchair user — and I would bring both the technical AR skills and the genuine motivation to make the project successful. I have Swift experience and have worked with BLE peripherals in a previous mobile development module.',
 'pending', '2026-03-14 10:50:00', '2026-03-14 10:50:00'),

(20, 11, 37,
 'I am interested in both augmented reality and accessibility design, and this project sits exactly at that intersection. I have Android development experience with Kotlin and have read extensively about ARCore''s environmental understanding APIs. I would be particularly enthusiastic about the usability testing component, as I completed a module on inclusive design and have experience recruiting and running user studies with participants with disabilities.',
 'pending', '2026-03-16 16:20:00', '2026-03-16 16:20:00'),

-- Project 43 (Embedded Vision — requested) — 1 pending
(21, 11, 43,
 'Embedded systems and computer vision are my two strongest technical areas. I completed a hardware module where I programmed a Raspberry Pi 4 for real-time image processing, and I have trained MobileNet models using TensorFlow Lite for an indoor plant disease detection hobby project. Applying this to PCB inspection in a manufacturing context is exactly the kind of applied engineering challenge I am looking for in my final-year project, and I am familiar with REST API development using FastAPI.',
 'pending', '2026-03-04 12:00:00', '2026-03-04 12:00:00'),

-- Project 44 (Multi-Robot Warehouse — agreed) — 1 accepted, 1 rejected
(22, 7,  44,
 'Robotics is my intended career path, and multi-robot systems are the subfield I find most compelling. I have completed ROS 2 tutorials, implemented a simple task scheduler for a two-robot simulation in CoppeliaSim, and studied the CBBA algorithm in a research paper reading group. I would relish the opportunity to compare centralised auction-based and decentralised CBBA approaches on a realistic warehouse scenario, and I am comfortable with both Python and C++ in the ROS 2 ecosystem.',
 'accepted', '2026-03-16 10:00:00', '2026-03-26 09:15:00'),

(23, 13, 44,
 'I applied because of my strong interest in robotics, but after receiving feedback from Prof. Liu I understand that Alex''s practical ROS 2 simulation experience makes him the better candidate at this stage.',
 'rejected', '2026-03-17 14:30:00', '2026-03-26 09:20:00'),

-- Project 47 (Human-Robot Safety — requested) — 2 pending
(24, 13, 47,
 'Industrial robot safety is a topic I have studied extensively after a site visit to a manufacturing plant in Year 2. I have implemented a basic pose estimation pipeline using MediaPipe and tested it on video footage of human movement, which is directly relevant to the collision risk prediction component. I am also familiar with ROS 2 and have a genuine interest in the ISO/TS 15066 standard, which I read as part of a self-study project on cobot deployment.',
 'pending', '2026-03-16 11:40:00', '2026-03-16 11:40:00'),

(25, 16, 47,
 'Safety in human-robot collaboration is something I find both technically interesting and socially important. I have a background in control systems from my electrical engineering modules and have implemented a simple obstacle avoidance algorithm on a TurtleBot 3 in ROS 2. I am keen to extend this into the safety monitoring domain, and I believe my control systems knowledge will be valuable when interfacing with the cobot arm model in this project.',
 'pending', '2026-03-18 09:50:00', '2026-03-18 09:50:00'),

-- Some withdrawn applications
(26, 13, 3,
 'I applied for this project before deciding to focus my final year on a different research direction. I am withdrawing my application and wish the other candidates well.',
 'withdrawn', '2026-03-02 10:00:00', '2026-03-04 16:00:00'),

(27, 7,  13,
 'Due to a change in my module selection for next year I need to withdraw this application. I am no longer able to commit to the data science focus this project requires.',
 'withdrawn', '2026-03-03 09:00:00', '2026-03-06 11:30:00');

-- ============================================================
-- NOTIFICATIONS (for accepted / rejected applicants)
-- ============================================================
INSERT INTO notifications (user_id, message, is_read, created_at) VALUES
(8,  'Application Accepted: Your application for "Natural Language Processing for Automated Essay Scoring" has been accepted. Please contact Dr. James Harrison to arrange your first meeting.',        0, '2026-03-18 09:01:00'),
(9,  'Application Update: Your application for "Natural Language Processing for Automated Essay Scoring" was not successful on this occasion. We encourage you to explore other available topics.',               0, '2026-03-18 09:06:00'),
(12, 'Application Accepted: Your application for "Big Data Pipeline for Real-Time Financial Market Risk Assessment" has been accepted. Please contact Dr. Sarah Chen to arrange your first meeting.', 0, '2026-03-22 08:31:00'),
(10, 'Application Accepted: Your application for "Secure Multi-Party Computation for Privacy-Preserving Collaborative Analytics" has been accepted.',                                                0, '2026-03-20 09:01:00'),
(14, 'Application Update: Your application for "Secure Multi-Party Computation for Privacy-Preserving Collaborative Analytics" was not successful. Please consider applying for other open topics.',             0, '2026-03-20 09:06:00'),
(8,  'Application Accepted: Your application for "Affective Computing Interface for Student Health Monitoring" has been accepted. Please contact Dr. Emily Roberts for next steps.',        0, '2026-03-25 08:46:00'),
(7,  'Application Accepted: Your application for "Decentralised Task Allocation for Multi-Robot Warehouse Automation" has been accepted. Please contact Prof. David Liu to arrange your first meeting.', 0, '2026-03-26 09:16:00'),
(13, 'Application Update: Your application for "Decentralised Task Allocation for Multi-Robot Warehouse Automation" was not successful on this occasion.',                                                       0, '2026-03-26 09:21:00');
