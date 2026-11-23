import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import * as L from 'leaflet';
import { LatLng } from '../../core/models/ride.model';

export interface MapMarker {
  id: string;
  lat: number;
  lng: number;
  label?: string;
  color?: string;
}

/** Leaflet map on OpenStreetMap tiles. Pass stable array/object references to avoid needless redraws. */
@Component({
  selector: 'app-map-view',
  standalone: true,
  template: `<div #host class="host"></div>`,
  styles: [
    `
      :host {
        display: block;
        height: 100%;
        min-height: 260px;
      }
      .host {
        height: 100%;
        min-height: 260px;
      }
    `,
  ],
})
export class MapViewComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() center: LatLng = { lat: 17.385, lng: 78.4867 };
  @Input() zoom = 14;
  @Input() markers: MapMarker[] = [];
  /** Zoom to fit all markers instead of following `center`. */
  @Input() fit = false;
  @Input() selectable = false;
  @Output() mapClick = new EventEmitter<LatLng>();

  @ViewChild('host', { static: true }) host!: ElementRef<HTMLDivElement>;

  private map?: L.Map;
  private readonly layer = L.layerGroup();

  ngAfterViewInit(): void {
    this.map = L.map(this.host.nativeElement).setView([this.center.lat, this.center.lng], this.zoom);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);
    this.layer.addTo(this.map);
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      if (this.selectable) {
        this.mapClick.emit({ lat: e.latlng.lat, lng: e.latlng.lng });
      }
    });
    this.render();
    setTimeout(() => this.map?.invalidateSize(), 0);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.map) {
      return;
    }
    if (changes['center'] && !this.fit) {
      this.map.setView([this.center.lat, this.center.lng], this.map.getZoom());
    }
    this.render();
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  private render(): void {
    if (!this.map) {
      return;
    }
    this.layer.clearLayers();
    for (const m of this.markers) {
      const color = m.color ?? '#2158c4';
      const tag = m.label ? `<span class="tag">${escapeHtml(m.label)}</span>` : '';
      const icon = L.divIcon({
        className: 'sanchari-pin',
        html: `<span class="dot" style="background:${color}"></span>${tag}`,
        iconSize: [18, 18],
        iconAnchor: [9, 9],
      });
      L.marker([m.lat, m.lng], { icon }).addTo(this.layer);
    }
    if (this.fit && this.markers.length > 1) {
      const bounds = L.latLngBounds(this.markers.map((m) => [m.lat, m.lng] as [number, number]));
      this.map.fitBounds(bounds.pad(0.3));
    } else if (this.fit && this.markers.length === 1) {
      this.map.setView([this.markers[0].lat, this.markers[0].lng], this.zoom);
    }
  }
}

function escapeHtml(s: string): string {
  return s.replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c] as string);
}
