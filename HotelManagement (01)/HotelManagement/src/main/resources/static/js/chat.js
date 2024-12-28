let stompClient = null;

document.addEventListener('DOMContentLoaded', function () {
    connectWebSocket();

    $('#chat-form').submit(function(event) {
        event.preventDefault();
        const messageContent = $('#message-input').val().trim();
        if (messageContent) {
            sendMessage('CHAT_MESSAGE', messageContent);
            showMessage(messageContent, true);
            $('#message-input').val('');
        }
    });
});

function connectWebSocket() {
    const socket = new SockJS('/hotel-chat');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, frame => {
        console.log('Connected:', frame);
        stompClient.subscribe('/topic/messages', response => {
            showResponse(JSON.parse(response.body));
        });
        showSystemMessage("Kết nối thành công! Bạn có thể bắt đầu tìm phòng.");
    }, error => {
        console.error('STOMP error:', error);
        showSystemMessage("Có lỗi xảy ra khi kết nối.");
    });
}

function sendMessage(type, content) {
    if (stompClient && stompClient.connected) {
        console.log('Sending message:', { type, content });
        stompClient.send("/app/chat", {}, JSON.stringify({ type, content }));
    } else {
        showSystemMessage("Chưa kết nối đến server. Vui lòng thử lại sau.");
    }
}

async function showResponse(message) {
    const response = message.response;

    if (Array.isArray(response) && response.length > 0) {
        showSystemMessage("Tìm thấy phòng phù hợp:");
        for (const room of response) {
            showMessage(createRoomCard(room), false);
        }
    } else if (typeof response === 'string') {
        showSystemMessage(response);
    } else if (typeof response === 'object' && response !== null && response.id) {
        showSystemMessage("Tìm thấy phòng phù hợp:");
        showMessage(createRoomCard(response), false);
    } else {
        showSystemMessage("Không tìm thấy phòng phù hợp.");
    }
}

function createRoomCard(room) {
    if (!room || !room.roomType) return '';

    return `
        <div class="bg-white p-4 rounded-lg shadow mb-3">
            <h4 class="font-bold text-lg">Phòng ${room.roomNumber}</h4>
            <div class="grid grid-cols-2 gap-2 mt-2">
                <p><span class="font-medium">Loại phòng:</span> ${room.roomType.name}</p>
                <p><span class="font-medium">Giá:</span> ${formatPrice(room.price)}</p>
                <p><span class="font-medium">Sức chứa:</span> ${room.capacity} người</p>
                <p><span class="font-medium">Trạng thái:</span> ${formatStatus(room.status)}</p>
            </div>
            ${room.imageUrl ? `
                <a href="/room/${room.id}" target="_blank">
                    <img src="${room.imageUrl}" alt="Room image" class="mt-3 rounded w-full h-48 object-cover"/>
                </a>` : ''}
            ${room.roomType.amenities ? `
                <div class="mt-3">
                    <p class="font-medium">Tiện nghi:</p>
                    <div class="flex flex-wrap gap-2 mt-1">
                        ${room.roomType.amenities.map(amenity => `<span class="bg-blue-100 text-blue-800 text-sm px-2 py-1 rounded">${amenity.name}</span>`).join('')}
                    </div>
                </div>` : ''}
        </div>
    `;
}


function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(price);
}

function formatStatus(status) {
    const statusMap = {
        'AVAILABLE': 'Còn trống',
        'OCCUPIED': 'Đã đặt',
        'MAINTENANCE': 'Bảo trì'
    };
    return statusMap[status] || status;
}

function findByPrice() {
    const price = prompt("Nhập giá tối đa (VND):");
    if (price && !isNaN(price)) {
        sendMessage('ROOM_PRICE', price);
    } else {
        showSystemMessage("Giá nhập vào không hợp lệ. Vui lòng nhập lại.");
    }
}



function findByRoomNumber() {
    const roomNumber = prompt("Nhập số phòng:");
    if (roomNumber && !isNaN(roomNumber)) {
        sendMessage('ROOM_NUMBER', roomNumber);
    } else {
        showSystemMessage("Số phòng nhập vào không hợp lệ. Vui lòng nhập lại.");
    }
}

function showMessage(message, isUser) {
    const chatContent = document.getElementById('chat-content');
    const messageElement = document.createElement('div');
    messageElement.classList.add('message', isUser ? 'message-user' : 'message-bot');
    messageElement.classList.add(isUser ? 'text-right' : 'text-left'); 

    messageElement.innerHTML = `<div class="message-text">${message}</div>`;
    chatContent.appendChild(messageElement);
    chatContent.scrollTop = chatContent.scrollHeight;
}

function showSystemMessage(message) {
    showMessage(message, false);
}