import {createStore, applyMiddleware} from 'redux';
import {ShopReducer} from './ShopReducer';
import {CartReducer} from './CartReducer';
import {CommonReducer} from './CommonReducer';
import {asyncActions} from './AsyncMiddleWare';

export const SportsStoreDataStore = createStore(CommonReducer(ShopReducer, CartReducer), applyMiddleware(asyncActions));