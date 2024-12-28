package com.example.hotelmanager.Controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hotelmanager.model.Amenities;
import com.example.hotelmanager.model.ChatMessage;
import com.example.hotelmanager.model.News;
import com.example.hotelmanager.model.Room;
import com.example.hotelmanager.repository.RoomTypeRepository;
import com.example.hotelmanager.service.NewsService;
import com.example.hotelmanager.service.RoomService;

import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/")
public class HomePageController {
	
	private final RoomService roomService;
	private final RoomTypeRepository roomTypeRepository;
    private final NewsService newsService;

	@Autowired
	public HomePageController(NewsService newsSerice,RoomService roomService, RoomTypeRepository roomTypeRepository)	{
		this.roomService = roomService;
		this.roomTypeRepository = roomTypeRepository;
        this.newsService = newsSerice;

	}
	
	 @GetMapping
	    public String getAllRooms(Model model) {
	        List<Room> rooms = roomService.getAllRooms();
	        model.addAttribute("rooms", rooms);
	        List<News> newsList = newsService.getAllNews();
	        model.addAttribute("newsList", newsList);
	        // Lấy dữ liệu
	        List<String> locations = roomService.getDistinctLocations();
	        List<Integer> capacities = roomService.getDistinctCapacities();
	        List<String> statuses = roomService.getDistinctStatuses();
	        List<Amenities> amenities = roomService.getAllAmenities(); 
	        model.addAttribute("locations", locations);
	        model.addAttribute("capacities", capacities);
	        model.addAttribute("statuses", statuses);
	        model.addAttribute("amenities", amenities);

	        return "pages/home_page"; 
	    }

	//detail
	    @GetMapping("/room/{id}")
	    public String getRoomDetail(@PathVariable("id") Long id, Model model) {
	        Room room = roomService.getRoomById(id);

	        model.addAttribute("room", room);
	        
	        // Lấy các tiện ích từ loại phòng
	        if (room.getRoomType() != null) {
	            model.addAttribute("amenities", room.getRoomType().getAmenities());
	        } else {
	            model.addAttribute("amenities", null); // hoặc một danh sách rỗng
	        }

	        return "room/detail";
	    }
	    
	    //chat 

	    @Transactional
	    @MessageMapping("/chat")
	    @SendTo("/topic/messages")
	    public ChatMessage handleMessage(ChatMessage message) {
	        String type = message.getType();
	        String content = message.getContent();

	        Object response = null;
	        switch (type) {
		        case "CHAT_MESSAGE":
		        	response = processGeneralQuestion(content);
		        	break;
	        	
	            case "ROOM_PRICE":
	                double price = Double.parseDouble(content);
	                response = roomService.findRoomsByPriceLessThan(price);
	                break;
	            
	            case "ROOM_NUMBER":
	                int roomNumber = Integer.parseInt(content);
	                response = roomService.findRoomByRoomNumber(roomNumber);
	                break;
	            default:
	        }

	        ChatMessage responseMessage = new ChatMessage();
	        responseMessage.setType("ROOM_SEARCH_RESULT");
	        responseMessage.setResponse(response);
	        return responseMessage;
	    }
	    
	    private String processGeneralQuestion(String question) {
	        question = question.toLowerCase().trim();
	        
	        if (question.contains("giá") || question.contains("chi phí")) {
	            return "Để tìm bạn có thể sử dụng nút 'Tìm theo giá tối đa'";
	        }	        
	        
	        if (question.contains("số phòng") || question.contains("phòng số")) {
	            return "Để tìm phòng bạn có thể sử dụng nút 'Tìm theo số phòng'";
	        }

	        if (question.contains("giờ nhận phòng") || question.contains("giờ trả phòng")) {
	            return "Giờ nhận phòng là 14:00 và giờ trả phòng là 12:00.";
	        }

	        if (question.contains("chính sách hủy")) {
	            return "Chính sách hủy phòng tùy thuộc vào loại phòng và điều kiện đặt phòng. Bạn có thể kiểm tra khi đặt phòng.";
	        }

	        if (question.contains("đặt phòng")) {
	            return "Bạn có thể đặt phòng thông qua trang web hoặc liên hệ với chúng tôi qua số điện thoại.";
	        }

	        if (question.contains("có bãi đỗ xe không") || question.contains("đỗ xe")) {
	            return "Chúng tôi có bãi đỗ xe miễn phí cho khách hàng. Vui lòng liên hệ với lễ tân để biết thêm chi tiết.";
	        }

	        if (question.contains("wifi")) {
	            return "Chúng tôi cung cấp dịch vụ wifi miễn phí cho tất cả các phòng.";
	        }

	        if (question.contains("thời gian làm việc của lễ tân") || question.contains("lễ tân")) {
	            return "Lễ tân làm việc 24/7 để phục vụ bạn bất kỳ lúc nào.";
	        }

	        return "Xin chào! Tôi có thể giúp gì cho bạn. Nếu bạn muốn tìm phòng hãy sử dụng nút (Tìm theo giá) hoặc (Tìm theo giá tối đa) ";
	    }
}
