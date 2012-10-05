/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * <h1>Repos Restclient</h1>
 * <p>
 * Simple interface for stateless HTTP communication using GET, HEAD and (future) POST.
 * </p>
 * <p>
 * There is one central interface se.repos.restclient.RestClient.
 * This interface is composed of RestGetClient, RestHeadClient...
 * When services declare dependencies to those single-method interfaces,
 * the REST interactions become easy to mock using anonymous inner classes.
 * </p>
 * <p>
 * The primary methods have String argument for the resource URL '''without hostname'''.
 * This means that the target server and authentication is set in the constructor.
 * </p>
 * Clients may however extend RestClientMultiHostBase which implements RestClientMultiHost.
 * Those implementations can support the same methods with URL arguments.
 * </p>
 */
package se.repos.restclient;
