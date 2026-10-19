import { Routes } from '@angular/router';
import { adminGuard, authGuard } from './core/auth.guard';
import { AuthPage } from './pages/auth-page';
import { CatalogPage } from './pages/catalog-page';
import { CollectionsPage } from './pages/collections-page';
import { UsersPage } from './pages/users-page';
import { TypesPage } from './pages/types-page';

export const routes: Routes = [
	{ path: 'login', component: AuthPage, data: { mode: 'login' } },
	{ path: 'register', component: AuthPage, data: { mode: 'register' } },
	{ path: 'collections', component: CollectionsPage, canActivate: [authGuard] },
	{ path: 'catalog', component: CatalogPage, canActivate: [authGuard] },
	{ path: 'types', component: TypesPage, canActivate: [authGuard] },
	{ path: 'users', component: UsersPage, canActivate: [authGuard, adminGuard] },
	{ path: '', pathMatch: 'full', redirectTo: 'collections' },
	{ path: '**', redirectTo: 'collections' },
];
