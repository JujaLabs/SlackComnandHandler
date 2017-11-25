package ua.com.juja.slack.command.handler.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ua.com.juja.slack.command.handler.exception.ParseSlackCommandException;

import java.util.*;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;

/**
 * @author Konstantin Sergey
 * @author Nikolay Horushko
 */
public class SlackParsedCommandTest {

    private UserData fromUser;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        fromUser = new UserData("uuid0", "UFDR97JLA");
    }

    @Test
    public void getFirstUserInTextIfOneSlackNameInText() {
        //given
        final String text = "text <@U1DR97JLA|slackName1> text";
        final UserData userInText = new UserData("uuid1", "U1DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText);
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        UserData result = slackParsedCommand.getFirstUserFromText();
        //then
        assertEquals(userInText, result);
    }

    @Test
    public void getFirstUserInTextIfSomeSlackNameInText() {
        //given
        final String text = "text text <@U1DR97JLA|slackName1> text <@U2DR97JLA|slackName2> text <@U3DR97JLA|slackName3>";
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final UserData userInText2 = new UserData("uuid2", "U2DR97JLA");
        final UserData userInText3 = new UserData("uuid3", "U3DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1, userInText2, userInText3);
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        UserData result = slackParsedCommand.getFirstUserFromText();
        //then
        assertEquals(userInText1, result);
    }

    @Test
    public void getFirstUserInTextIfOneSlackNameInTextWithoutSpace() {
        //given
        final String text = "text text<@U1DR97JLA|slackName1> text text";
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1);
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        UserData result = slackParsedCommand.getFirstUserFromText();
        //then
        assertEquals(userInText1, result);
    }

    @Test
    public void getFirstUserInTextThrowExceptionIfNotUser() {
        //given
        final String text = "text text text";
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, new ArrayList<>());
        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("The text 'text text text' doesn't contain any slack names"));
        //when
        slackParsedCommand.getFirstUserFromText();
    }

    @Test
    public void getAllUsers() {
        //given
        final String text = "text <@U3DR97JLA|slackName3> text<@U2DR97JLA|slackName2> text <@U1DR97JLA|slackName1>";
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final UserData userInText2 = new UserData("uuid2", "U2DR97JLA");
        final UserData userInText3 = new UserData("uuid3", "U3DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1, userInText2, userInText3);
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        List<UserData> result = slackParsedCommand.getAllUsersInText();
        //then
        assertEquals(usersInText, result);
    }

    @Test
    public void getAllUsersIfNotSlackNameInText() {
        //given
        final String text = "text text text";
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, Collections.emptyList());
        //when
        List<UserData> result = slackParsedCommand.getAllUsersInText();
        //then
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void getText() {
        //given
        final String text = "text";
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, Collections.emptyList());
        //when
        String result = slackParsedCommand.getText();
        //then
        assertEquals("text", result);
    }

    @Test
    public void getFromUser() {
        //given
        final String text = "text";
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, Collections.emptyList());
        //when
        UserData result = slackParsedCommand.getFromUser();
        //then
        assertEquals(fromUser, result);
    }

    @Test
    public void getUserCountIfOneUser() {
        //given
        final String text = "text <@U1DR97JLA|slackName1> text";
        final List<UserData> usersInText = Arrays.asList(new UserData("uuid1", "U1DR97JLA"));
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        assertEquals(1, slackParsedCommand.getUserCountInText());
    }

    @Test
    public void getUserCountIfNotUser() {
        //given
        final String text = "text text";
        //when
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, Collections.emptyList());
        //then
        assertEquals(0, slackParsedCommand.getUserCountInText());
    }

    @Test
    public void getUserCountIfManyUsers() {
        //given
        final String text = "text <@U1DR97JLA|slackName1> text <@U2DR97JLA|slackName2> text <@U3DR97JLA|slackName3>";
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final UserData userInText2 = new UserData("uuid2", "U2DR97JLA");
        final UserData userInText3 = new UserData("uuid3", "U3DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1, userInText2, userInText3);
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        assertEquals(3, slackParsedCommand.getUserCountInText());
    }

    @Test
    public void getTextWithoutSlackNames() {
        //given
        final String text = "<@U1DR97JLA|slackName1> text  <@U1DR97JLA|slackName1> text <@U1DR97JLA|slackName1>";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, Collections.emptyList());
        //then
        assertEquals("text text", slackParsedCommand.getTextWithoutSlackNames());
    }

    @Test
    public void getTextWithoutSlackNamesTrimSpaces() {
        //given
        final String text = "    <@U1DR97JLA|slackName1> text  <@U1DR97JLA|slackName1> text <@U1DR97JLA|slackName1>    ";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, Collections.emptyList());
        //then
        assertEquals("text text", slackParsedCommand.getTextWithoutSlackNames());
    }

    @Test
    public void getTextWithoutSlackNamesIfNotSlackNameInText() {
        //given
        final String text = "text  text";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, Collections.emptyList());
        //then
        assertEquals("text text", slackParsedCommand.getTextWithoutSlackNames());
    }

    @Test
    public void getUsersByTokensWithTwoTokensSuccessfullCases() {
        //given
        final List<String> textList = new ArrayList<>(Arrays.asList(
                "-t1 <@U1DR97JLA|slackName1> -t2 <@U2DR97JLA|slackName2>",
                "-t2 <@U2DR97JLA|slackName2> -t1 <@U1DR97JLA|slackName1>",
                "-t1@<@U1DR97JLA|slackName1> -t2@<@U2DR97JLA|slackName2>",
                "text -t2 <@U2DR97JLA|slackName2> text -t1 <@U1DR97JLA|slackName1> text",
                "text -t2 <@U2DR97JLA|slackName2> text -t1 <@U1DR97JLA|slackName1> text",
                "text -t2 <@U2DR97JLA|slackName2> -t1text <@U1DR97JLA|slackName1> text"
        ));
        final Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2"}));
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final UserData userInText2 = new UserData("uuid2", "U2DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1, userInText2);

        final Map<String, UserData> expectedResult = new LinkedHashMap<>();
        expectedResult.put("-t1", userInText1);
        expectedResult.put("-t2", userInText2);

        textList.forEach(text -> {
            SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
            //when
            Map<String, UserData> result = slackParsedCommand.getUsersWithTokens(tokens);
            //then
            assertEquals(expectedResult.size(), result.size());
            assertEquals(expectedResult, result);
        });
    }

    @Test
    public void getUsersByTokensWithThreeTokensSuccessfullCases() {
        //given
        //<@U1DR97JLA|slackName1> <@U2DR97JLA|slackName2> <@U3DR97JLA|slackName3>
        final List<String> textList = new ArrayList<>(Arrays.asList(
                "-t1 <@U1DR97JLA|slackName1> -t2 <@U2DR97JLA|slackName2> -t3 <@U3DR97JLA|slackName3>",
                "-t1<@U1DR97JLA|slackName1> -t2 <@U2DR97JLA|slackName2> -t3 <@U3DR97JLA|slackName3>",
                "-t1<@U1DR97JLA|slackName1> -t2<@U2DR97JLA|slackName2> -t3<@U3DR97JLA|slackName3>",
                "-t1 <@U1DR97JLA|slackName1> -t3 <@U3DR97JLA|slackName3> -t2 <@U2DR97JLA|slackName2>",
                "-t1<@U1DR97JLA|slackName1> -t3<@U3DR97JLA|slackName3> -t2<@U2DR97JLA|slackName2>",
                "-t1<@U1DR97JLA|slackName1> text -t3<@U3DR97JLA|slackName3> text -t2<@U2DR97JLA|slackName2> text",
                "-t1 text <@U1DR97JLA|slackName1> text -t3 text <@U3DR97JLA|slackName3> text -t2 text <@U2DR97JLA|slackName2> text",
                "-t1 text<@U1DR97JLA|slackName1> text -t3 text<@U3DR97JLA|slackName3> text -t2 text<@U2DR97JLA|slackName2> text",
                "text -t2 <@U2DR97JLA|slackName2> -t1 text <@U1DR97JLA|slackName1> text -t3 <@U3DR97JLA|slackName3>"
        ));
        final Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2", "-t3"}));
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final UserData userInText2 = new UserData("uuid2", "U2DR97JLA");
        final UserData userInText3 = new UserData("uuid3", "U3DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1, userInText2, userInText3);

        final Map<String, UserData> expectedResult = new LinkedHashMap<>();
        expectedResult.put("-t1", userInText1);
        expectedResult.put("-t2", userInText2);
        expectedResult.put("-t3", userInText3);

        textList.forEach(text -> {
            //when
            SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
            Map<String, UserData> result = slackParsedCommand.getUsersWithTokens(tokens);
            //then
            assertEquals(expectedResult.size(), result.size());
            assertEquals(expectedResult, result);
        });
    }

    @Test
    public void getUsersByTokensIfTokensArrayIsEmpty() {
        //given
        final String text = "text <@U1DR97JLA|slackName1> text <@U2DR97JLA|slackName2> text <@U3DR97JLA|slackName3>";
        final Set<String> tokens = new HashSet<>();
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final UserData userInText2 = new UserData("uuid2", "U2DR97JLA");
        final UserData userInText3 = new UserData("uuid3", "U3DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1, userInText2, userInText3);

        final Map<String, UserData> expectedResult = new LinkedHashMap<>();

        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        Map<String, UserData> result = slackParsedCommand.getUsersWithTokens(tokens);
        //then
        assertEquals(expectedResult.size(), result.size());
        assertEquals(expectedResult, result);
    }

    @Test
    public void getUsersByTokensThrowExceptionIfTokenNotFound() {
        //given
        final String text = "text-t2<@U2DR97JLA|slackName2> text<@U1DR97JLA|slackName1> text";
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final UserData userInText2 = new UserData("uuid2", "U2DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1, userInText2);
        final Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2"}));
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);

        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("Token '-t1' didn't find in the string 'text-t2<@U2DR97JLA|slackName2> text<@U1DR97JLA|slackName1> text'"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensErrorIfNotFoundSlackNameForToken() {
        //given
        final String text = "text-t2 <@U2DR97JLA|slackName2> text -t1 text";
        final Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2"}));
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final UserData userInText2 = new UserData("uuid2", "U2DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1, userInText2);
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("The text 'text-t2 <@U2DR97JLA|slackName2> text -t1 text' doesn't " +
                "contain slackName for token '-t1'"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensError2() {
        //given
        final String text = "text-t2 -t1<@U2DR97JLA|slackName2> text text";
        final Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2"}));
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final UserData userInText2 = new UserData("uuid2", "U2DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1, userInText2);
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);

        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("The text 'text-t2 -t1<@U2DR97JLA|slackName2> text text' doesn't contain " +
                "slackName for token '-t2'"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensError3() {
        //given
        final String text = "-t1 <@U1DR97JLA|slackName1> -t2 -t3 <@U2DR97JLA|slackName2> -t3 <@U3DR97JLA|slackName3>";
        final Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2", "-t3"}));
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final UserData userInText2 = new UserData("uuid2", "U2DR97JLA");
        final UserData userInText3 = new UserData("uuid3", "U3DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1, userInText2, userInText3);
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("The text '-t1 <@U1DR97JLA|slackName1> -t2 -t3 <@U2DR97JLA|slackName2> -t3 <@U3DR97JLA|slackName3>'" +
                " contains 2 tokens '-t3', but expected 1"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensErrorTextContainsMoreThanOneToken() {
        //given
        final String text = "text-t2 -t1<@U2DR97JLA|slackName2> text -t1 text";
        final Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2"}));
        final UserData userInText1 = new UserData("uuid1", "U1DR97JLA");
        final UserData userInText2 = new UserData("uuid2", "U2DR97JLA");
        final List<UserData> usersInText = Arrays.asList(userInText1, userInText2);
        final SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("The text 'text-t2 -t1<@U2DR97JLA|slackName2> text -t1 text' contains 2 tokens '-t1'," +
                " but expected 1"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }
}