/**
 * Created by clauzewitz on 2016. 1. 28..
 */
//var URL_BASE = "http://104.155.225.112:8080/recipe/rapi";
URL_BASE = "http://localhost:8080/recipe/rapi";

angular.module('starter.services', [])

    .service('MainService', function ($http, $q) {
        this.getSampleList = function (sampleReq) {
            var dfd = $q.defer();
            var url = URL_BASE + "/admin/recipe/list";
//            $http({method: 'POST', url:url, headers:{"auth_token" :"3886f45b6d7246119d07e44c8f4cfb7d7aa17cacc906dc9265dab575130a8fa7"},
//            	data: sampleReq })
//           	.then(function successCallback(result) {
//                	console.log("==== Result 결과값 로그=====");
//           		console.log(result);
//                   if (result.code == 0) {
//                   	console.log("====Service Result.Data");
//                       dfd.resolve(result.data);
//                   } else {
//                   	console.log("====Service Result.Message");
//                        dfd.reject(result.message);
//                        //console.log(result.message);
//                    }
//            	}, function errorCallback(data) {
//            		console.log(data);
//           		console.log(status);
//            		dfd.reject(data.message);
//            	});
            
            $http.post(url, sampleReq,
            	
            	{ headers:{"auth_token" :"3886f45b6d7246119d07e44c8f4cfb7d7aa17cacc906dc9265dab575130a8fa7"},
//            	withCredentials:true		
            	}
            ).success(function (result) {
            		console.log("==== Result 결과값 로그=====");
            		console.log(result);
            		
                    if (result.code == 0) {
                        dfd.resolve(result.data);
                    } else {
                       dfd.reject(result.message);
                    }
                    
                })
                .error(function (data, status) {
                    console.log(data);
                    console.log(status);
                    dfd.reject(data.message);
                });
            return dfd.promise;
        }
        
    });