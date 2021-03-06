package io.stock.evaluation.web.ticker.stock.external;

import io.stock.evaluation.web.ticker.stock.dto.TickerStockDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import reactor.core.publisher.Flux;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class DartDataLoader {

    // 다트 제공 xml 파일을 Flux<TickerMetaItem> 으로 변환한다.
    // 추후 Spring Batch 기반의 별도 모듈로 분리 예정
    public Flux<TickerStockDto> processTickers() {
        Optional<Path> tickerXmlPath = getDartTickerListXmlPath();
        if(tickerXmlPath.isEmpty()) return Flux.empty();

        return parseNodeList(tickerXmlPath.get())
                .map(list -> {  // NodeList가 비어있지 않다면, 아래의 작업을 수행, Flux<TickerMetaItem> 을 생성해낸다.
                    return Flux
                            .range(0, list.getLength())          // 0 ~ length 만큼 데이터의 흐름을 만들어서
                            .map(list::item)                            // 각각의 list.item(i) 를 수행하고
                            .filter(node -> isValidNode.test(node))     // ELEMENT_NODE 타입이면서, 비어있지 않은 노드만 선택해서
                            .map(node -> newTickerItem.apply(node));    // node 의 각 요소를 떼어서 TickerMetaItem 을 생성한다.
                })
                .orElse(Flux.empty()); // 그 외의 경우는 Flux.empty() 를 리턴한다.
    }

    protected Optional<Path> getDartTickerListXmlPath(){
        try {
            String rootDir = Paths.get(ClassLoader.getSystemResource("dart").toURI()).toAbsolutePath().toString();
            return Optional.ofNullable(Paths.get(rootDir, "CORPCODE.xml"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    protected Optional<NodeList> parseNodeList(Path tickerXmlPath){
        try(InputStream inputStream = Files.newInputStream(tickerXmlPath)){
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(inputStream);
            return Optional.ofNullable(document.getElementsByTagName("list"));
        }
        catch(ParserConfigurationException | SAXException | IOException e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

    Function<Node, String> getTextContent = node -> {
        return Optional
                .ofNullable(node.getTextContent())
                .orElse("");
    };

    Function<Node, TickerStockDto> newTickerItem = (node) -> {
        NodeList childNodes = node.getChildNodes();
        Node corpCode = childNodes.item(1);
        Node corpName = childNodes.item(3);
        Node stockCode = childNodes.item(5);
        return new TickerStockDto(getTextContent.apply(stockCode), getTextContent.apply(corpName), getTextContent.apply(corpCode));
    };

    Predicate<Node> isValidNode = node -> {
        // ELEMENT_NODE 타입이면서
        if(node.getNodeType() == Node.ELEMENT_NODE){
            // stockCode 가 없는 item 도 있기에 stockCode 가 존재하는 아이템일 경우에만 true 를 리턴
            if(StringUtils.hasText(node.getChildNodes().item(5).getTextContent())){
                return true;
            }
            return false;
        }
        return false;
    };

    // 종목 추천의 경우, 미리 생성된 종목명에 대한 형태소들을 넣어주어야 하기때문에, 비동기가 아닌 동기방식으로.
    public List<TickerStockDto> processTickersBlock(){
        Optional<Path> tickerXmlPath = getDartTickerListXmlPath();
        if(tickerXmlPath.isEmpty()) return new ArrayList<>();

        return parseNodeList(tickerXmlPath.get())
                .map(list -> {
                    return IntStream.range(0, list.getLength())
                            .boxed()
                            .map(list::item)
                            .filter(node -> isValidNode.test(node))
                            .map(node -> newTickerItem.apply(node))
                            .collect(Collectors.toList());
                })
                .orElse(new ArrayList<>());
    }

}
