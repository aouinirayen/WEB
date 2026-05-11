import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class GeminiService {

  generateContractClauses(
    partnerName: string, partnerSector: string, orgName: string,
    orgType: string, contractType: string, durationMonths: number
  ): Observable<any> {
    const clauses = this.buildClauses(partnerName, partnerSector, orgName, orgType, contractType, durationMonths);
    return of({ candidates: [{ content: { parts: [{ text: JSON.stringify({ clauses }) }] } }] });
  }

  private buildClauses(partnerName: string, partnerSector: string, orgName: string, orgType: string, contractType: string, durationMonths: number): any[] {
    const clauses = [
      { numero: 1, titre: "Contract Subject",
        contenu: `This ${contractType} partnership agreement is entered into between ${orgName}, a public transport operator of type ${orgType} in Tunisia, and ${partnerName}, an active player in the ${partnerSector} sector. This partnership aims to establish a framework of mutual collaboration to improve public transport services across Tunisian territory, in accordance with the provisions of Tunisian public transport law no. 2004-33.` },
      { numero: 2, titre: "Duration and Renewal",
        contenu: `This contract takes effect on the date of signing and is concluded for a duration of ${durationMonths} months. Upon expiration of this period, the contract may be renewed by mutual agreement of the parties, subject to a written notice of 30 days prior to the expiration date. ${orgName} and ${partnerName} undertake to respect the notification deadlines for any renewal or termination.` },
      { numero: 3, titre: "Obligations of Parties",
        contenu: `${orgName} undertakes to provide ${partnerName} with privileged access to its ${orgType} transport network and to actively collaborate for the development of services. In return, ${partnerName} undertakes to bring its expertise in the ${partnerSector} field, to respect TransitTN service quality standards, and to maintain the confidentiality of information exchanged within the framework of this partnership.` },
      { numero: 4, titre: "Financial Conditions and Billing",
        contenu: `The financial terms of this ${contractType} partnership will be defined in a separate pricing annex, jointly signed by the legal representatives of both parties. Payments will be made according to a quarterly schedule, with invoices issued in accordance with Tunisian tax regulations. Any payment delay exceeding 30 days will result in the application of penalties in accordance with the legal rate in force in Tunisia.` },
      { numero: 5, titre: "Dispute Resolution and Applicable Law",
        contenu: `In case of dispute between ${orgName} and ${partnerName} regarding the interpretation or execution of this contract, the parties undertake to seek an amicable solution within 30 days. Failing an amicable agreement, the dispute will be submitted to the competent jurisdiction of Tunis, in accordance with the Tunisian law in force. This contract is governed by Tunisian legislation and the TransitTN platform shall act as a first-instance mediator.` }
    ];

    if (partnerSector === 'TECHNOLOGY' || partnerSector === 'FINANCIAL') {
      clauses.push({
        numero: 6,
        titre: partnerSector === 'TECHNOLOGY' ? "Technology Integration and Data Security" : "Payment Terms and Financial Services",
        contenu: partnerSector === 'TECHNOLOGY'
          ? `${partnerName} undertakes to integrate its technology solutions with the information system of ${orgName} in compliance with ISO 27001 security standards. Data of public transport users collected within the framework of this partnership will be processed in accordance with the Tunisian law on personal data protection.`
          : `${partnerName} undertakes to facilitate financial transactions related to the services of ${orgName} in compliance with the regulations of the Central Bank of Tunisia. The terms of commission, settlement deadlines and bank guarantees will be defined in accordance with the BCT circular applicable to public transport partnerships.`
      });
    }

    return clauses;
  }
}