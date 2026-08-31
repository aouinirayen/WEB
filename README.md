# OpenStack Private Cloud — Transport Management Platform (IaaS → PaaS → SaaS)

A private cloud platform built on OpenStack, delivering the full cloud service model — from infrastructure (IaaS) up to a deployed SaaS application for transport management in Tunisia. The OpenStack cluster is fully automated with Ansible, and a containerized full-stack application runs on top of it inside an OpenStack virtual machine.

> Academic integrated project (PI) covering the three cloud service layers: IaaS, PaaS and SaaS.

---

## Overview

The project provisions a multi-node OpenStack cluster from scratch, automates its deployment with Ansible, and uses it as the foundation to host a real application — a transport management platform for Tunisia. It demonstrates the complete path from bare infrastructure to a running software service:

- **IaaS** — the OpenStack cluster itself: compute, networking, storage and image services.
- **PaaS** — orchestrated, containerized runtime provisioned on top of the cloud.
- **SaaS** — the transport management application delivered to end users.

---

## Cluster Architecture

```
                    ┌─────────────────────────────┐
                    │        Controller node       │
                    │  Keystone · Horizon · Nova   │
                    │  Neutron · Glance · Heat     │
                    └──────────────┬──────────────┘
                                   │
        ┌──────────────┬──────────┼──────────┬──────────────┐
        ▼              ▼          ▼          ▼              ▼
   ┌─────────┐   ┌─────────┐ ┌─────────┐ ┌─────────┐   ┌──────────┐
   │ Compute │   │ Compute │ │ Compute │ │ Compute │   │  Storage │
   │  node 1 │   │  node 2 │ │  node 3 │ │  node 4 │   │ (Cinder) │
   └─────────┘   └─────────┘ └─────────┘ └─────────┘   └──────────┘

   4× compute nodes (Nova)              1× block-storage node (Cinder)
```

**Topology:** 1 controller · 4 compute nodes · 1 storage node.

---

## OpenStack Services

| Service   | Role |
|-----------|------|
| **Keystone** | Identity & authentication |
| **Nova**     | Compute (VM lifecycle) |
| **Neutron**  | Networking (tenant networks, routing, security groups) |
| **Glance**   | VM images |
| **Cinder**   | Block storage |
| **Horizon**  | Web dashboard |
| **Heat**     | Orchestration (Infrastructure-as-Code templates) |

---

## Infrastructure Automation

The entire cluster is deployed and configured with **Ansible**, covering:

- Provisioning and configuration of controller, compute and storage nodes.
- Installation and wiring of the OpenStack services above.
- Repeatable, idempotent setup of the full cluster.

Networking is configured with **tenant networks, routing, security groups and floating IPs** for external access to instances.

**Heat** is used for orchestration, defining infrastructure resources as declarative templates.

---

## Hosted Application (SaaS layer)

On top of the cloud runs a full-stack **transport management platform for Tunisia**, deployed inside an OpenStack virtual machine:

- **Frontend:** Angular
- **Backend:** Spring Boot (REST API)
- **Database:** MySQL
- **Packaging:** Docker containers running on the provisioned VM

This is the SaaS layer of the project — a working application delivered on the private cloud built underneath it.

---

## Tech Stack

**Cloud:** OpenStack (Keystone, Nova, Neutron, Glance, Cinder, Horizon, Heat)
**Automation / IaC:** Ansible · Heat
**Networking:** tenant networks, routing, security groups, floating IPs
**Application:** Angular · Spring Boot · MySQL · Docker
**OS:** Linux

---

## Key Takeaways

- Deployed a multi-node OpenStack cluster from scratch and automated it end-to-end with Ansible.
- Configured core OpenStack services and networking (tenant networks, routing, security groups).
- Delivered the complete cloud service model — IaaS, PaaS and SaaS — on a single platform.
- Debugged deployment issues across nodes using logs and CLI tools.

---

## Author

**Rayen Aouini** — Cloud & DevOps Engineering Student
[LinkedIn](https://linkedin.com/in/aouinirayen) · [GitHub](https://github.com/aouinirayen)
