import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-otp-input',
  standalone: true,
  template: `
    <input
      class="input otp"
      type="text"
      inputmode="numeric"
      autocomplete="one-time-code"
      [attr.maxlength]="length"
      [placeholder]="'•'.repeat(length)"
      [value]="value"
      (input)="onInput($event)"
    />
  `,
  styles: [
    `
      .otp { letter-spacing: 0.5em; text-align: center; font-size: 1.4rem; font-weight: 700; }
    `,
  ],
})
export class OtpInputComponent {
  @Input() length = 6;
  @Input() value = '';
  @Output() valueChange = new EventEmitter<string>();
  @Output() completed = new EventEmitter<string>();

  onInput(event: Event): void {
    const el = event.target as HTMLInputElement;
    const digits = el.value.replace(/\D/g, '').slice(0, this.length);
    el.value = digits;
    this.value = digits;
    this.valueChange.emit(digits);
    if (digits.length === this.length) {
      this.completed.emit(digits);
    }
  }
}
