(function() {
    var httpRequest;
    document.getElementById("sendText").onclick = function() {
        var chatText = document.getElementById("chatText").value;
        makeRequest(chatText);
    };

    function makeRequest(chatText) {
        httpRequest = new XMLHttpRequest();

        if (!httpRequest) {
            alert('Giving up :( Cannot create an XMLHTTP instance');
            return false;
        }
        httpRequest.onreadystatechange = alertContents;
        httpRequest.open('POST', '/chat');
        httpRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        httpRequest.send('chatText=' + encodeURIComponent(chatText));
        httpRequest.send();
    }

    function alertContents() {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                alert(httpRequest.responseText);
            } else {
                alert('There was a problem with the request.');
            }
        }
    }
})();

//
// document.getElementById("sendText").onclick = function() {
//     var chatText = document.getElementById("chatText").value;
//     chat(chatText);
// };
//
// function chat() {
//     httpRequest = new XMLHttpRequest();
//
//
//
//     $.ajax({
//         type: 'POST',
//         url: '/chat',
//         data: $('#chatForm').serialize(), //JSON.stringify({text: text}),
//         success: function (data) {
//             alert('data: ' + data);
//         },
//         contentType: "application/json",
//         dataType: 'json'
//     });
// }
//
// // $(document).ready(function() {
// //     // document.getElementById("sendText").onclick = function () {
// //     //     var text = document.getElementById("chatText").value;
// //     //     chat(text);
// //     // };
// //
// //     $('#sendText').click(function (event) {
// //         $.post('/chat', $('#chatForm').serialize(),
// //             function (data) {
// //                 alert('data: ' + data);
// //                 //$('.success_msg').append("Vote Successfully Recorded").fadeOut();
// //             }
// //         );
// //         event.preventDefault();
// //     });
// //
// //         // $.ajax({
// //         //     type: 'POST',
// //         //     url: '/chat',
// //         //     data: $('#chatForm').serialize(), //JSON.stringify({text: text}),
// //         //     success: function (data) {
// //         //         alert('data: ' + data);
// //         //     },
// //         //     contentType: "application/json",
// //         //     dataType: 'json'
// //         // });
// //
// //     //})
// //
// //     // function chat() {
// //     //     $.ajax({
// //     //         type: 'POST',
// //     //         url: '/chat',
// //     //         data: $('#chatForm').serialize(), //JSON.stringify({text: text}),
// //     //         success: function (data) {
// //     //             alert('data: ' + data);
// //     //         },
// //     //         contentType: "application/json",
// //     //         dataType: 'json'
// //     //     });
// //     // }
// // });