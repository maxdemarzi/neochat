(function() {
    var httpRequest;
    var chatWindow = document.getElementById("chatWindow");
    var objDiv = document.getElementById("scroll");
    var sendText = document.getElementById("sendText").onclick = function(evt) {
        var chatText = document.getElementById("chatText").value;
        makeRequest(chatText);
        evt.preventDefault();
    };

    function makeRequest(chatText) {
        httpRequest = new XMLHttpRequest();

        if (!httpRequest) {
            alert('Giving up :( Cannot create an XMLHTTP instance');
            return false;
        }

        httpRequest.onreadystatechange = updateChat;
        httpRequest.open('POST', '/chat', true);
        httpRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        httpRequest.send('chatText=' + encodeURIComponent(chatText));
    }

    function updateChat() {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {

                var li = document.createElement("li");
                li.setAttribute('class', 'left');
                var content = document.createElement('div');
                content.setAttribute('class', 'content');
                li.appendChild(content);
                var message = document.createElement('div');
                message.setAttribute('class',  'message');
                content.appendChild(message);
                var bubble = document.createElement('div');
                bubble.setAttribute('class', 'bubble');
                message.appendChild(bubble);
                var p = document.createElement('p');
                p.textContent = chatText.value;
                bubble.appendChild(p);

                chatWindow.appendChild(li);

                var responseTextList = JSON.parse(httpRequest.responseText);
                responseTextList.forEach(
                    function (item, index) {
                        li = document.createElement("li");
                        content = document.createElement('div');
                        content.setAttribute('class', 'content');
                        li.appendChild(content);
                        message = document.createElement('div');
                        message.setAttribute('class',  'message');
                        content.appendChild(message);
                        bubble = document.createElement('div');
                        bubble.setAttribute('class', 'bubble');
                        message.appendChild(bubble);
                        p = document.createElement('p');
                        p.textContent = item.response;
                        bubble.appendChild(p);
                        chatWindow.appendChild(li);
                });
            } else {
                alert('There was a problem with the request.');
            }
            objDiv.scrollTop = objDiv.scrollHeight;
        }
    }

})();