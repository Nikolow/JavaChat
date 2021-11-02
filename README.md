# Description
This is a simple Chat Server-Client without any GUI. Users must be at the same network - they are connecting based by ports.

<center>

![Image](https://cloud.netlifyusercontent.com/assets/344dbf88-fdf9-42bb-adb4-46f01eedd629/6c3ead2d-a453-4c41-ac54-2823b27dd966/hr-ross-cooper-2.png)

</center>

# Functionality
* DB Connection
> Using: JDBC Driver
> Init function: Create tables if they do not exist

<br>

* Server
> Using: ServerSocket
 Functions: Login, Register, Last Messages, Help Text, Commands
 
<br>

* Client
> Using: ServerSocket
 Functions: Send Message / Command, Read Message

<center>

![Image](https://cloud.netlifyusercontent.com/assets/344dbf88-fdf9-42bb-adb4-46f01eedd629/6c3ead2d-a453-4c41-ac54-2823b27dd966/hr-ross-cooper-2.png)

</center>

# Function: Login
- Validations
- Check and get last messages to the account after login

<center>

![Image](https://cloud.netlifyusercontent.com/assets/344dbf88-fdf9-42bb-adb4-46f01eedd629/6c3ead2d-a453-4c41-ac54-2823b27dd966/hr-ross-cooper-2.png)

</center>

# Function: Logout
- Validation
- Simple command

<center>

![Image](https://cloud.netlifyusercontent.com/assets/344dbf88-fdf9-42bb-adb4-46f01eedd629/6c3ead2d-a453-4c41-ac54-2823b27dd966/hr-ross-cooper-2.png)

</center>

# Function: Last Messages
- Simple query with limit
- Print the messages in DESC

<center>

![Image](https://cloud.netlifyusercontent.com/assets/344dbf88-fdf9-42bb-adb4-46f01eedd629/6c3ead2d-a453-4c41-ac54-2823b27dd966/hr-ross-cooper-2.png)

</center>

# Function: List
- Show online users logged in


<center>

![Image](https://cloud.netlifyusercontent.com/assets/344dbf88-fdf9-42bb-adb4-46f01eedd629/6c3ead2d-a453-4c41-ac54-2823b27dd966/hr-ross-cooper-2.png)

</center>

# Function: Help Text
- Show the text on each login
- Show usage of the commands

<center>

![Image](https://cloud.netlifyusercontent.com/assets/344dbf88-fdf9-42bb-adb4-46f01eedd629/6c3ead2d-a453-4c41-ac54-2823b27dd966/hr-ross-cooper-2.png)

</center>

# Function: Commands
- Command: Login -> Login Function
- Command: Logout -> Logout Function
- Command: List -> Online Users
- Command: History -> Last Messages with limit
- Command: Help -> Help Text
- Send Messages
