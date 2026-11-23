import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'inr', standalone: true })
export class CurrencyInrPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    if (value === null || value === undefined) {
      return '—';
    }
    return '₹' + new Intl.NumberFormat('en-IN', { maximumFractionDigits: 0 }).format(value);
  }
}
