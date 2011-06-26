= Repos Restclient =

Simple interface for stateless HTTP communication using GET, HEAD and (future) POST.

There is one central interface se.repos.restclient.RestClient.
This interface is composed of RestGetClient, RestHeadClient...

The primary methods have String argument for the resource URL '''without hostname'''.
This means that the target server and authentication is set in the constructor.

Clients may however extend RestClientMultiHostBase which implements RestClientMultiHost.
Those implementations can support the same methods with URL arguments.



