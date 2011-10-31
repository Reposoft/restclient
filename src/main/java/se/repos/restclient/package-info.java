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
