/**
 * Copyright 2017 IBM All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an 'AS IS' BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
'use strict';
var log4js = require('log4js');
var logger = log4js.getLogger('SampleWebApp');
var express = require('express');
var session = require('express-session');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var http = require('http');
var util = require('util');
var app = express();
var cors = require('cors');
var config = require('./config.json');
var helper = require('./app/helper.js');
var channels = require('./app/create-channel.js');
var join = require('./app/join-channel.js');
var install = require('./app/install-chaincode.js');
var instantiate = require('./app/instantiate-chaincode.js');
var invoke = require('./app/invoke-transaction.js');
var query = require('./app/query.js');
var host = process.env.HOST || config.host;
var port = process.env.PORT || config.port;
///////////////////////////////////////////////////////////////////////////////
//////////////////////////////// SET CONFIGURATONS ////////////////////////////
///////////////////////////////////////////////////////////////////////////////
app.options('*', cors());
app.use(cors());
//support parsing of application/json type post data
app.use(bodyParser.json());
//support parsing of application/x-www-form-urlencoded post data
app.use(bodyParser.urlencoded({
	extended: false
}));


///////////////////////////////////////////////////////////////////////////////
//////////////////////////////// START SERVER /////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
var server = http.createServer(app).listen(port, function() {});
logger.info('****************** SERVER STARTED ************************');
logger.info('**************  http://' + host + ':' + port +
	'  ******************');
server.timeout = 240000;

function getErrorMessage(field) {
	var response = {
		success: false,
		message: field + ' field is missing or Invalid in the request'
	};
	return response;
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////// REST ENDPOINTS START HERE ///////////////////////////
///////////////////////////////////////////////////////////////////////////////
// Register and enroll user
app.post('/users', function(req, res) {
	var username = req.body.username;
	var orgname = req.body.orgname;
	logger.debug('End point : /users');
	logger.debug('User name : ' + username);
	logger.debug('Org name  : ' + orgname);
	if (!username) {
		res.json(getErrorMessage('\'username\''));
		return;
	}
	if (!orgname) {
		res.json(getErrorMessage('\'orgname\''));
		return;
	}

	helper.getRegisteredUsers(username, orgname, true).then(function(response) {
		if (response && typeof response !== 'string') {
			res.json({
				success: true,
				message: "Success"
			});
		} else {
			res.json({
				success: false,
				message: "failed"
			});
		}
	});
});
// Create Channel
app.post('/channels', function(req, res) {
	logger.info('<<<<<<<<<<<<<<<<< C R E A T E  C H A N N E L >>>>>>>>>>>>>>>>>');
	logger.debug('End point : /channels');
	var channelName = req.body.channelName;
	var channelConfigPath = req.body.channelConfigPath;
	logger.debug('Channel name : ' + channelName);
	logger.debug('channelConfigPath : ' + channelConfigPath); 
	if (!channelName) {
		res.json(getErrorMessage('\'channelName\''));
		return;
	}
	if (!channelConfigPath) {
		res.json(getErrorMessage('\'channelConfigPath\''));
		return;
	}

	channels.createChannel(channelName, channelConfigPath, req.body.username, req.body.orgname)
	.then(function(message) {
		res.json(message);
	});
});
// Join Channel
app.post('/channels/peers', function(req, res) {
	logger.info('<<<<<<<<<<<<<<<<< J O I N  C H A N N E L >>>>>>>>>>>>>>>>>');
	var channelName = req.body.channelName;
	var peers = req.body.peers;
	logger.debug('channelName : ' + channelName);
	logger.debug('peers : ' + peers);
	if (!channelName) {
		res.json(getErrorMessage('\'channelName\''));
		return;
	}
	if (!peers || peers.length == 0) {
		res.json(getErrorMessage('\'peers\''));
		return;
	}

	join.joinChannel(channelName, peers, req.body.username, req.body.orgname)
	.then(function(message) {
		res.json(message);
	});
});
// Install chaincode on target peers
app.post('/chaincodes', function(req, res) {
	logger.debug('==================== INSTALL CHAINCODE ==================');
	var peers = req.body.peers;
	var chaincodeName = req.body.chaincodeName;
	var chaincodePath = req.body.chaincodePath;
	var chaincodeVersion = req.body.chaincodeVersion;
	logger.debug('peers : ' + peers); // target peers list
	logger.debug('chaincodeName : ' + chaincodeName);
	logger.debug('chaincodePath  : ' + chaincodePath);
	logger.debug('chaincodeVersion  : ' + chaincodeVersion);
	if (!peers || peers.length == 0) {
		res.json(getErrorMessage('\'peers\''));
		return;
	}
	if (!chaincodeName) {
		res.json(getErrorMessage('\'chaincodeName\''));
		return;
	}
	if (!chaincodePath) {
		res.json(getErrorMessage('\'chaincodePath\''));
		return;
	}
	if (!chaincodeVersion) {
		res.json(getErrorMessage('\'chaincodeVersion\''));
		return;
	}

	install.installChaincode(peers, chaincodeName, chaincodePath, chaincodeVersion, req.body.username, req.body.orgname)
	.then(function(message) {
		res.json({
			success: true,
			message: message
		});
	});
});
// Instantiate chaincode on target peers
app.post('/channels/chaincodes', function(req, res) {
	logger.debug('==================== INSTANTIATE CHAINCODE ==================');
	var chaincodeName = req.body.chaincodeName;
	var chaincodeVersion = req.body.chaincodeVersion;
	var channelName = req.body.channelName;
	var functionName = req.body.functionName;
	var args = req.body.args;
	logger.debug('channelName  : ' + channelName);
	logger.debug('chaincodeName : ' + chaincodeName);
	logger.debug('chaincodeVersion  : ' + chaincodeVersion);
	logger.debug('functionName  : ' + functionName);
	logger.debug('args  : ' + args);
	if (!chaincodeName) {
		res.json(getErrorMessage('\'chaincodeName\''));
		return;
	}
	if (!chaincodeVersion) {
		res.json(getErrorMessage('\'chaincodeVersion\''));
		return;
	}
	if (!channelName) {
		res.json(getErrorMessage('\'channelName\''));
		return;
	}
	if (!functionName) {
		res.json(getErrorMessage('\'functionName\''));
		return;
	}
	if (!args) {
		res.json(getErrorMessage('\'args\''));
		return;
	}
	instantiate.instantiateChaincode(channelName, chaincodeName, chaincodeVersion, functionName, args, req.body.username, req.body.orgname)
	.then(function(message) {
		res.json({
			success: true,
			message: message
		});
	});
});
// Invoke transaction on chaincode on target peers
app.post('/channels/chaincodes/invoke', function(req, res) {
	logger.debug('==================== INVOKE ON CHAINCODE ==================');
	var peers = req.body.peers;
	var chaincodeName = req.body.chaincodeName;
	var channelName = req.body.channelName;
	var fcn = req.body.functionName;
	var args = req.body.args;
	logger.debug('channelName  : ' + channelName);
	logger.debug('chaincodeName : ' + chaincodeName);
	logger.debug('fcn  : ' + fcn);
	logger.debug('args  : ' + args);
	if (!peers || peers.length == 0) {
		res.json(getErrorMessage('\'peers\''));
		return;
	}
	if (!chaincodeName) {
		res.json(getErrorMessage('\'chaincodeName\''));
		return;
	}
	if (!channelName) {
		res.json(getErrorMessage('\'channelName\''));
		return;
	}
	if (!fcn) {
		res.json(getErrorMessage('\'fcn\''));
		return;
	}
	if (!args) {
		res.json(getErrorMessage('\'args\''));
		return;
	}

	invoke.invokeChaincode(peers, channelName, chaincodeName, fcn, args, req.body.username, req.body.orgname)
	.then(function(message) {
            if(message == "Failed to order the transaction. Error code: undefined"){
		res.json({
			success: false,
			message: message
		});
            } else {
                res.json({
                        success: true,
                        message: message
                });
            }
	});
});
// Query transaction on chaincode on target peer
app.post('/channels/chaincodes/query', function(req, res) {
	logger.debug('==================== QUERY ON CHAINCODE ==================');
	var peer = req.body.peer;
	var chaincodeName = req.body.chaincodeName;
	var channelName = req.body.channelName;
	var fcn = req.body.functionName;
	var args = req.body.args;
	logger.debug('channelName  : ' + channelName);
	logger.debug('peer  : ' + peer);
	logger.debug('chaincodeName : ' + chaincodeName);
	logger.debug('fcn  : ' + fcn);
	logger.debug('args  : ' + args);

	if (!chaincodeName) {
		res.json(getErrorMessage('\'chaincodeName\''));
		return;
	}
	if (!channelName) {
		res.json(getErrorMessage('\'channelName\''));
		return;
	}
	if (!fcn) {
		res.json(getErrorMessage('\'fcn\''));
		return;
	}
	if (!args) {
		res.json(getErrorMessage('\'args\''));
		return;
	}

	query.queryChaincode(peer, channelName, chaincodeName, args, fcn, req.body.username, req.body.orgname)
	.then(function(message) {
		// res.json({
		// 	success: true,
		// 	message: message
		// });
	    if(message == "Failed to order the transaction. Error code: undefined"){
                res.json({
                        success: false,
                        message: JSON.parse(message)
                });
            } else {
                res.json({
                        success: true,
                        message: JSON.parse(message)
                });
            }

//		res.json({
//			success: true,
//			message: JSON.parse(message)
//		});
	});
});

app.post('/channels/transactions/txid', function(req, res) {
	logger.debug(
		'================ GET TRANSACTION BY TRANSACTION_ID ======================'
	);
	logger.debug('channelName : ' + req.body.channelName);
	let trxnId = req.body.args[0];
	//let peer = req.query.peer;
	if (!trxnId) {
		res.json(getErrorMessage('\'trxnId\''));
		return;
	}

	query.getTransactionByID("peer1", trxnId, req.body.username, req.body.orgname)
		.then(function(message) {
			//res.send(message);
		    if(message == "Failed to order the transaction. Error code: undefined"){
                        res.json({
                             sucess:false,
                             message:message
                        })
                    } else{
                        res.json({
                                success:true,
                                signature:message.transactionEnvelope.signature,
                                timestamp:message.transactionEnvelope.payload.header.channel_header.timestamp,
                                action:message.transactionEnvelope.payload.data.actions[0].payload.action.proposal_response_payload.extension.results,
                                responseResult:message.transactionEnvelope.payload.data.actions[0].payload.action.proposal_response_payload.extension.response
                        })
                    }
		});
});
// //  Query Get Block by BlockNumber
// app.get('/channels/:channelName/blocks/:blockId', function(req, res) {
// 	logger.debug('==================== GET BLOCK BY NUMBER ==================');
// 	let blockId = req.params.blockId;
// 	let peer = req.query.peer;
// 	logger.debug('channelName : ' + req.params.channelName);
// 	logger.debug('BlockID : ' + blockId);
// 	logger.debug('Peer : ' + peer);
// 	if (!blockId) {
// 		res.json(getErrorMessage('\'blockId\''));
// 		return;
// 	}
// 	query.getBlockByNumber(peer, blockId, req.body.username, req.body.orgname)
// 		.then(function(message) {
// 			res.send(message);
// 		});
// });
// Query Get Transaction by Transaction ID
// // Query Get Block by Hash
// app.get('/channels/:channelName/blocks', function(req, res) {
// 	logger.debug('================ GET BLOCK BY HASH ======================');
// 	logger.debug('channelName : ' + req.params.channelName);
// 	let hash = req.query.hash;
// 	let peer = req.query.peer;
// 	if (!hash) {
// 		res.json(getErrorMessage('\'hash\''));
// 		return;
// 	}

// 	query.getBlockByHash(peer, hash, req.body.username, req.body.orgname).then(
// 		function(message) {
// 			res.send(message);
// 		});
// });
// //Query for Channel Information
// app.get('/channels/:channelName', function(req, res) {
// 	logger.debug(
// 		'================ GET CHANNEL INFORMATION ======================');
// 	logger.debug('channelName : ' + req.params.channelName);
// 	let peer = req.query.peer;

// 	query.getChainInfo(peer, req.body.username, req.body.orgname).then(
// 		function(message) {
// 			res.send(message);
// 		});
// });
// // Query to fetch all Installed/instantiated chaincodes
// app.get('/chaincodes', function(req, res) {
// 	var peer = req.query.peer;
// 	var installType = req.query.type;
// 	//TODO: add Constnats
// 	if (installType === 'installed') {
// 		logger.debug(
// 			'================ GET INSTALLED CHAINCODES ======================');
// 	} else {
// 		logger.debug(
// 			'================ GET INSTANTIATED CHAINCODES ======================');
// 	}

// 	query.getInstalledChaincodes(peer, installType, req.body.username, req.body.orgname)
// 	.then(function(message) {
// 		res.send(message);
// 	});
// });
// // Query to fetch channels
// app.get('/channels', function(req, res) {
// 	logger.debug('================ GET CHANNELS ======================');
// 	logger.debug('peer: ' + req.query.peer);
// 	var peer = req.query.peer;
// 	if (!peer) {
// 		res.json(getErrorMessage('\'peer\''));
// 		return;
// 	}

// 	query.getChannels(peer, req.body.username, req.body.orgname)
// 	.then(function(
// 		message) {
// 		res.send(message);
// 	});
// });
