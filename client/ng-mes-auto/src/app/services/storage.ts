import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class Storage {
  setItem = (key, value) => localStorage.setItem(key, JSON.stringify(value));
  getItem = (key) => JSON.parse(localStorage.getItem(key));
  removeItem = (key) => localStorage.removeItem(key);
  clear = () => localStorage.clear();

}
