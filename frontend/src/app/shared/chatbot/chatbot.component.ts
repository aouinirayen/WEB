import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService }  from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { RoleEnum }    from '../../core/models/models';
import { forkJoin } from 'rxjs';
import { environment } from '../../../environments/environment';


interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  loading?: boolean;
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styles: [`
    /* ── FLOATING BUTTON ── */
    .chat-fab {
      position: fixed; bottom: 28px; right: 28px; z-index: 1000;
      width: 56px; height: 56px; border-radius: 50%;
      background: linear-gradient(135deg, #1a73e8, #0d47a1);
      border: none; cursor: pointer; box-shadow: 0 4px 16px rgba(26,115,232,0.4);
      display: flex; align-items: center; justify-content: center;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    .chat-fab:hover { transform: scale(1.08); box-shadow: 0 6px 20px rgba(26,115,232,0.5); }
    .chat-fab i { color: #fff; font-size: 22px; }
    .chat-fab .fab-badge {
      position: absolute; top: -2px; right: -2px;
      width: 18px; height: 18px; border-radius: 50%;
      background: #c62828; color: #fff; font-size: 0.62rem; font-weight: 700;
      display: flex; align-items: center; justify-content: center; border: 2px solid #fff;
    }

    /* ── CHAT WINDOW ── */
    .chat-window {
      position: fixed; bottom: 96px; right: 28px; z-index: 1000;
      width: 380px; height: 520px;
      background: #fff; border-radius: 16px;
      box-shadow: 0 12px 40px rgba(0,0,0,0.18);
      border: 1px solid #e5e7eb;
      display: flex; flex-direction: column; overflow: hidden;
      animation: slideUp 0.2s ease;
    }
    @keyframes slideUp {
      from { opacity:0; transform:translateY(16px); }
      to   { opacity:1; transform:translateY(0); }
    }

    /* ── HEADER ── */
    .chat-header {
      padding: 14px 18px;
      background: linear-gradient(135deg, #1a237e, #1a73e8);
      display: flex; align-items: center; justify-content: space-between;
    }
    .chat-header-left { display:flex; align-items:center; gap:10px; }
    .chat-bot-avatar {
      width: 36px; height: 36px; border-radius: 50%;
      background: rgba(255,255,255,0.2);
      display: flex; align-items: center; justify-content: center;
    }
    .chat-bot-avatar i { color:#fff; font-size:16px; }
    .chat-header-info h4 { color:#fff; font-size:0.9rem; font-weight:700; margin:0; }
    .chat-header-info span { color:rgba(255,255,255,0.7); font-size:0.72rem; }
    .chat-online-dot {
      width:8px; height:8px; border-radius:50%; background:#4caf50;
      display:inline-block; margin-right:4px;
    }
    .chat-close-btn {
      width:28px; height:28px; border-radius:6px;
      background:rgba(255,255,255,0.15); border:none; cursor:pointer;
      color:#fff; font-size:13px; display:flex; align-items:center; justify-content:center;
      transition:background 0.15s;
    }
    .chat-close-btn:hover { background:rgba(255,255,255,0.25); }

    /* ── MESSAGES ── */
    .chat-messages {
      flex:1; overflow-y:auto; padding:16px;
      display:flex; flex-direction:column; gap:12px;
      background:#f9fafb;
    }
    .chat-messages::-webkit-scrollbar { width:4px; }
    .chat-messages::-webkit-scrollbar-track { background:transparent; }
    .chat-messages::-webkit-scrollbar-thumb { background:#d1d5db; border-radius:2px; }

    .msg { display:flex; gap:8px; align-items:flex-end; }
    .msg.user { flex-direction:row-reverse; }

    .msg-avatar {
      width:28px; height:28px; border-radius:50%; flex-shrink:0;
      display:flex; align-items:center; justify-content:center; font-size:11px; font-weight:700;
    }
    .msg.assistant .msg-avatar { background:#e3f2fd; color:#1a73e8; }
    .msg.user      .msg-avatar { background:#1a73e8; color:#fff; }

    .msg-bubble {
      max-width:75%; padding:10px 14px; border-radius:12px;
      font-size:0.83rem; line-height:1.5;
    }
    .msg.assistant .msg-bubble {
      background:#fff; color:#1f2937;
      border-bottom-left-radius:4px;
      box-shadow:0 1px 4px rgba(0,0,0,0.08);
    }
    .msg.user .msg-bubble {
      background:linear-gradient(135deg,#1a73e8,#0d47a1); color:#fff;
      border-bottom-right-radius:4px;
    }

    /* Loading dots */
    .loading-dots span {
      display:inline-block; width:6px; height:6px; border-radius:50%;
      background:#1a73e8; margin:0 2px; animation:bounce 1.2s infinite;
    }
    .loading-dots span:nth-child(2) { animation-delay:0.2s; }
    .loading-dots span:nth-child(3) { animation-delay:0.4s; }
    @keyframes bounce {
      0%,80%,100% { transform:translateY(0); }
      40%         { transform:translateY(-6px); }
    }

    /* ── SUGGESTIONS ── */
    .chat-suggestions {
      padding:8px 14px; display:flex; gap:6px; flex-wrap:wrap;
      border-top:1px solid #f0f0f0; background:#fff;
    }
    .suggestion-chip {
      padding:4px 10px; border-radius:12px; font-size:0.72rem; font-weight:600;
      background:#e3f2fd; color:#1a73e8; border:1px solid #bbdefb;
      cursor:pointer; transition:all 0.15s; white-space:nowrap;
    }
    .suggestion-chip:hover { background:#1a73e8; color:#fff; }

    /* ── INPUT ── */
    .chat-input-bar {
      padding:12px 14px; background:#fff;
      border-top:1px solid #e5e7eb;
      display:flex; gap:8px; align-items:center;
    }
    .chat-input {
      flex:1; border:1px solid #e5e7eb; border-radius:20px;
      padding:8px 14px; font-size:0.83rem; font-family:inherit;
      outline:none; transition:border-color 0.15s; resize:none;
      max-height:80px; overflow-y:auto;
    }
    .chat-input:focus { border-color:#1a73e8; }
    .chat-send-btn {
      width:36px; height:36px; border-radius:50%; border:none; cursor:pointer;
      background:linear-gradient(135deg,#1a73e8,#0d47a1);
      color:#fff; font-size:14px; display:flex; align-items:center; justify-content:center;
      transition:all 0.15s; flex-shrink:0;
    }
    .chat-send-btn:hover { transform:scale(1.08); }
    .chat-send-btn:disabled { opacity:0.5; cursor:not-allowed; transform:none; }
  `],
  template: `
    <!-- FAB BUTTON -->
    <button class="chat-fab" (click)="toggleChat()" *ngIf="!isOpen">
      <i class="fas fa-robot"></i>
    </button>

    <!-- CHAT WINDOW -->
    <div class="chat-window" *ngIf="isOpen">

      <!-- Header -->
      <div class="chat-header">
        <div class="chat-header-left">
          <div class="chat-bot-avatar"><i class="fas fa-robot"></i></div>
          <div class="chat-header-info">
            <h4>TransitTN Assistant</h4>
            <span><span class="chat-online-dot"></span>Online</span>
          </div>
        </div>
        <button class="chat-close-btn" (click)="toggleChat()">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <!-- Messages -->
      <div class="chat-messages" #messagesContainer>
        <div *ngFor="let msg of messages" class="msg" [ngClass]="msg.role">
          <div class="msg-avatar">
            <i *ngIf="msg.role === 'assistant'" class="fas fa-robot"></i>
            <span *ngIf="msg.role === 'user'">{{ userInitials }}</span>
          </div>
          <div class="msg-bubble">
            <div *ngIf="msg.loading" class="loading-dots">
              <span></span><span></span><span></span>
            </div>
            <span *ngIf="!msg.loading" [innerHTML]="formatMessage(msg.content)"></span>
          </div>
        </div>
      </div>

      <!-- Suggestion chips -->
      <div class="chat-suggestions" *ngIf="messages.length <= 1">
        <span *ngFor="let s of suggestions" class="suggestion-chip" (click)="sendSuggestion(s)">
          {{ s }}
        </span>
      </div>

      <!-- Input -->
      <div class="chat-input-bar">
        <textarea class="chat-input" [(ngModel)]="userInput" rows="1"
          placeholder="Ask me anything..."
          (keydown.enter)="onEnter($event)">
        </textarea>
        <button class="chat-send-btn" (click)="sendMessage()" [disabled]="isLoading || !userInput.trim()">
          <i class="fas" [ngClass]="isLoading ? 'fa-spinner fa-spin' : 'fa-paper-plane'"></i>
        </button>
      </div>
    </div>
  `
})
export class ChatbotComponent implements OnInit, AfterViewChecked {

  @ViewChild('messagesContainer') private msgContainer!: ElementRef;

  isOpen      = false;
  isLoading   = false;
  userInput   = '';
  userInitials = 'P';
  messages: ChatMessage[] = [];
  private systemPrompt    = '';
  private contextLoaded   = false;

  // Suggestions based on role
  suggestions: string[] = [];

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    const user = this.auth.getUser();
    if (user) {
      this.userInitials = this.auth.getInitials?.() || user.username?.charAt(0).toUpperCase() || 'U';
      this.loadContext(user.role as RoleEnum);
    }
    // Welcome message
    this.messages.push({
      role: 'assistant',
      content: '👋 Hello! I\'m the TransitTN Assistant. I can help you with your subscriptions, loyalty points, available plans, and more. How can I help you today?'
    });
  }

  ngAfterViewChecked() { this.scrollToBottom(); }

  // ── Load user context ─────────────────────────────────────────────────────

  private loadContext(role: RoleEnum): void {
    const userId = this.auth.getUserId();
    if (userId == null) return;

    if (role === RoleEnum.PASSENGER) {
      this.suggestions = [
        '📋 My subscriptions',
        '⭐ My loyalty points',
        '🚌 Available plans',
        '💳 How to use a promo code?'
      ];

      forkJoin({
        plans:  this.api.getAllPlans(),
        subs:   this.api.getMySubscriptions(userId),
        loyalty: this.api.getMyLoyalty(userId),
      }).subscribe({
        next: ({ plans, subs, loyalty }) => {
          const activeSubs  = subs.filter(s => s.statut === 'ACTIVE');
          const expiredSubs = subs.filter(s => s.statut === 'EXPIRED');
          const nextExpiry  = activeSubs.sort((a, b) =>
            new Date(a.dateFin).getTime() - new Date(b.dateFin).getTime()
          )[0];

          this.systemPrompt = `
You are TransitTN Assistant, a helpful chatbot for the TransitTN public transport subscription platform in Tunisia.
The user is a PASSENGER. 
CRITICAL RULE: Detect the language of EACH message and respond ONLY in that exact language.
French message → French response ONLY. English message → English response ONLY.
NEVER mix French and English in the same response.
Be concise, friendly, and helpful. Use emojis sparingly.

=== PASSENGER DATA ===
Name: ${this.auth.getUser()?.name || this.auth.getUser()?.username}
Loyalty points: ${loyalty.pointsCumules} pts (tier: ${loyalty.niveau})
Total subscriptions: ${subs.length}
Active subscriptions: ${activeSubs.length}
${nextExpiry ? `Next expiry: plan "${nextExpiry.pricingPlan?.nom}" on ${nextExpiry.dateFin}` : 'No active subscription'}

=== AVAILABLE PLANS (${plans.length} total) ===
${plans.map(p => `- ${p.nom} | ${p.type} | ${p.prix} DT | ${p.dureeEnJours} days | Transport: ${p.transportType || 'N/A'}`).join('\n')}

=== LOYALTY PROGRAM ===
- 10 points = 1 DT discount
- Tiers: BRONZE (0-199 pts), SILVER (200-499 pts), GOLD (500+ pts)
- Points earned = price paid in DT (rounded down)

=== PLATFORM FEATURES ===
- Subscribe to plans and pay via Stripe or loyalty points
- Apply promo codes at checkout
- Enable auto-renewal for automatic re-subscription
- Cancel active subscriptions anytime

If the user asks about something you don't have data for, suggest they check the relevant section of the app.
Never make up subscription details, plan prices, or point balances — only use the data provided above.
          `.trim();
          this.contextLoaded = true;
        },
        error: () => {
          this.systemPrompt  = this.defaultSystemPrompt('PASSENGER');
          this.contextLoaded = true;
        }
      });

    } else if (role === RoleEnum.OPERATOR) {
      this.suggestions = [
        '📊 My subscriptions stats',
        '⚠️ High-risk passengers',
        '🎯 ML insights',
        '💰 Average CLV'
      ];
       

      forkJoin({
  plans:  this.api.getPlansByOperator(userId),
  subs:   this.api.getSubscriptionsByOperator(userId),
  churn:  this.api.predictChurnAll(),
}).subscribe({
  next: ({ plans, subs, churn }) => {
    const usernames  = new Set(subs.map(s => s.passengerUsername).filter(Boolean));
    const myChurn    = churn.filter(c => usernames.has(c.username));
    const highRisk   = myChurn.filter(c => c.riskLevel === 'HIGH');
    const activeSubs = subs.filter(s => s.statut === 'ACTIVE');

    // ── Récupérer les IDs des passagers pour le CLV ──
    const passengerIds = [...new Set(
      subs.filter(s => s.passengerId != null).map(s => s.passengerId!)
    )];

    // ── Appel CLV pour chaque passager ──
    if (passengerIds.length > 0) {
      forkJoin(passengerIds.map(id => this.api.predictCLV(id))).subscribe({
        next: (clvResults) => {
          const vals   = clvResults.map(r => r.clvValue).filter(v => v > 0);
          const avgCLV = vals.length > 0
            ? (vals.reduce((a, b) => a + b, 0) / vals.length).toFixed(2)
            : '0';

          const clvDetails = clvResults
            .map(r => `  • ${r.username}: ${r.clvValue.toFixed(2)} DT — ${r.interpretation}`)
            .join('\n');

          this.systemPrompt = `
You are TransitTN Assistant for an OPERATOR on TransitTN.
Always respond in the same language as the user (French or English).
Be concise, data-driven, and professional.

=== OPERATOR DATA ===
Name: ${this.auth.getUser()?.name || this.auth.getUser()?.username}
Transport type: ${this.auth.getUser()?.transportType || 'N/A'}
Total pricing plans: ${plans.length}
Active subscriptions: ${activeSubs.length} / ${subs.length}
Unique passengers: ${usernames.size}

=== PRICING PLANS ===
${plans.map(p => `- ${p.nom} | ${p.type} | ${p.prix} DT | ${p.dureeEnJours} days`).join('\n')}

=== ML ANALYSIS ===
Total passengers analyzed: ${myChurn.length}
High-risk passengers (churn ≥ 70%): ${highRisk.length}
${highRisk.map(c => `  • ${c.username}: ${(c.churnProbability * 100).toFixed(0)}% — ${c.suggestedAction}`).join('\n')}

=== CUSTOMER LIFETIME VALUE (CLV) ===
Average CLV: ${avgCLV} DT (estimated over 12 months)
Details per passenger:
${clvDetails}

=== PLATFORM FEATURES ===
- Create and manage pricing plans
- Create discount codes for passengers
- View all subscriptions
- Access ML Analysis for full churn/CLV/recommendation data
- Send automated promo codes to high-risk passengers

Never make up data. Only use the information provided above.
          `.trim();
          this.contextLoaded = true;
        },
        error: () => {
          this.contextLoaded = true;
        }
      });
    } else {
      this.contextLoaded = true;
    }
  },
  error: () => {
    this.systemPrompt  = this.defaultSystemPrompt('OPERATOR');
    this.contextLoaded = true;
  }
});
     



    }
  }

  private defaultSystemPrompt(role: string): string {
  return `You are TransitTN Assistant for a ${role}.
Help with subscriptions, loyalty points, and transport plans in Tunisia.
CRITICAL: Detect the language of EACH user message and respond ONLY in that language.
If the user writes in French → respond ONLY in French.
If the user writes in English → respond ONLY in English.
NEVER mix languages in the same response.`;
}

  // ── Send message ──────────────────────────────────────────────────────────

  async sendMessage(): Promise<void> {
  const text = this.userInput.trim();
  if (!text || this.isLoading) return;

  this.userInput = '';
  this.messages.push({ role: 'user', content: text });
  const loadingMsg: ChatMessage = { role: 'assistant', content: '', loading: true };
  this.messages.push(loadingMsg);
  this.isLoading = true;

  try {
    const GROQ_KEY = environment.groqApiKey;


    // ── IMPORTANT : seulement les messages NON-vides et NON-loading ──
    const history = this.messages
      .filter(m => !m.loading && m.content && m.content.trim() !== '')
      .map(m => ({
        role:    m.role === 'user' ? 'user' : 'assistant',
        content: m.content.trim()
      }));

    console.log('Sending messages:', JSON.stringify(history));

    const response = await fetch('/groq-api/openai/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type':  'application/json',
        'Authorization': `Bearer ${GROQ_KEY}`
      },
      body: JSON.stringify({
        model:      'llama-3.3-70b-versatile',
        max_tokens: 800,
        messages: [
          {
            role:    'system',
            content: (this.systemPrompt || this.defaultSystemPrompt('USER')).trim()
          },
          ...history
        ],
      }),
    });

    const data = await response.json();
    console.log('Groq response:', data);

    const reply = data?.choices?.[0]?.message?.content
                  || data?.error?.message
                  || 'Sorry, I could not process your request.';

    const idx = this.messages.indexOf(loadingMsg);
    if (idx !== -1) this.messages[idx] = { role: 'assistant', content: reply };

  } catch (err) {
    console.error('Error:', err);
    const idx = this.messages.indexOf(loadingMsg);
    if (idx !== -1) this.messages[idx] = {
      role: 'assistant', content: '⚠️ Connection error. Please try again.'
    };
  }

  this.isLoading = false;
}

  sendSuggestion(s: string): void {
    this.userInput = s;
    this.sendMessage();
  }

  onEnter(event: KeyboardEvent): void {
    if (!event.shiftKey) { event.preventDefault(); this.sendMessage(); }
  }

  toggleChat(): void { this.isOpen = !this.isOpen; }

  formatMessage(text: string): string {
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g,     '<em>$1</em>')
      .replace(/\n/g,            '<br>');
  }

  private scrollToBottom(): void {
    try {
      const el = this.msgContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch {}
  }
}
